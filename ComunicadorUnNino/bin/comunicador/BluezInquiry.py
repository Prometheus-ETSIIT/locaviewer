# -*- coding: utf-8 -*-
#
###############################################
# Script para realizar un estudio de potencia #
# de un dispositivo Bluetooth.                #
#                                             #
# V 1.0: Benito Palacios Sánchez              #
#   - Implementación de descubrimiento básico #
# V 1.3: Benito Palacios Sánchez              #
#   - Añadido envío por sockets               #
#                                             #
# Copyright Prometheus 2014                   #
###############################################

import struct
import bluetooth._bluetooth as bluez
import bluetooth
import socket

# Información general:
#  Estructura de paquetes enviados (comandos): pág. 673
#  Estructura de paquetes recibidos (eventos): pág. 680
#  Documentación de bluez con comando de python: help(bluez)

# Implementación de búsqueda con RSSI con Bluez
class BluezInquiry:
    
    def __init__(self, dev_id, port):
        self.dev_id     = dev_id # El número del bluetooth (si hay más de uno)
        self.inquiring  = False
        self.socket     = None
        self.port       = port
        self.host       = 'localhost'
        self.sendSocket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

    def is_inquiring(self):
        return self.inquiring

    def create_socket(self):
   
	try:
            self.socket = bluez.hci_open_dev(self.dev_id)
        except:	        
            raise BluetoothError("Error al acceder al dispositivo")
            return

        # Establece el filtro para recibir todos los eventos
        flt = bluez.hci_filter_new()
        bluez.hci_filter_all_events(flt)
        bluez.hci_filter_set_ptype(flt, bluez.HCI_EVENT_PKT)
        try:
            # Establece opciones: nivel      nombre opción   valor
            self.socket.setsockopt(bluez.SOL_HCI, bluez.HCI_FILTER, flt)
        except:
           # raise BluetoothError("Problema al establecer filtro de eventos.")
	    print "error"
            return

    def inquiry(self):
        self.create_socket()
        if self.socket == None:
            return
           
        # Establece el modo de descubrimiento con RSSI (no extendido) - Pág. 682
        if self.write_inquiry_mode(1) == -1:
            return
            
        # TODO: Reemplazar por OCF_PERIODIC_INQUIRY y OCF_EXIT_PERIODIC_INQUIRY
        # Envía el paquete de Inquiry - Pág. 705
        max_period=[8,0] #Maximo periodo
        min_period=[7,0] #Minimo periodo
        duration = 6    # Valor óptimo según Tesis de Anne Franssens 
        max_resp = 255  # N. máximo respuestas (info dispositivos en un evento)
        # 0x9E8B33 -> General Inquiry Access Code
        # https://www.bluetooth.org/en-us/specification/assigned-numbers/baseband
        LAP = [ 0x33, 0x8B, 0x9E ]
        cmd_pkt = struct.pack("9B", max_period[0],max_period[1], min_period[0],min_period[1], LAP[0], LAP[1], LAP[2], duration, max_resp)
        bluez.hci_send_cmd(self.socket, bluez.OGF_LINK_CTL, bluez.OCF_PERIODIC_INQUIRY, cmd_pkt)

        self.inquiring = True
        
    def process_event(self):
        if self.inquiring == False:
            return
    
        # Leer 258 bytes (tamaño máximo: 3 cabecera + 255 datos)
        pkt = self.socket.recv(258)
        ptype, event, plen = struct.unpack("BBB", pkt[:3])
        pkt = pkt[3:] # Obtiene el payload (quitando cabecera)
        
        if event == bluez.EVT_INQUIRY_RESULT_WITH_RSSI:
            nrsp = struct.unpack("B", pkt[0])[0]    # Número de respuestas
            for i in range(nrsp):
                # Obtiene la dirección dispositivo
                addr = bluez.ba2str( pkt[1+6*i:1+6*i+6] )
                
                # Obtiene el rssi
                rssi = struct.unpack("b", pkt[1+13*nrsp+i])[0]
               # print addr, rssi
                #r = -0.00680102923817849*(int(rssi)**3) - 1.04905123190747*(int(rssi)**2) - 59.2087843354658*int(rssi) - 1106.35595941215
                #print r
                self.sendSocket.sendto(str(addr)+" "+str(rssi), (self.host, self.port))
		print str(addr)+" "+str(rssi)

        elif event == bluez.EVT_INQUIRY_COMPLETE:
            pass
            #self.socket.close()
            #self.socket = None
            #self.inquiring = False
            
        elif event == bluez.EVT_CMD_STATUS:
            status, ncmd, opcode = struct.unpack("BBH", pkt[:4])
            if status != 0:
                print "Dispositivo ocupado"
                self.socket.close()
                self.socket = None
                self.inquiring = False
        elif event == bluez.EVT_CMD_COMPLETE:
            pass        
        elif event == 255: #Suponemos que no lee ningun evento y por eso devuelve 255
            return
	elif event == bluez.EVT_INQUIRY_RESULT:
            nrsp = struct.unpack("B", pkt[0])[0]    # Número de respuestas
            for i in range(nrsp):
                addr = bluez.ba2str( pkt[1+6*i:1+6*i+6] )


                print "%s (no RRSI)" % addr
                
        else:
            self.inquiring = False
	    self.socket = None
            self.socket.close()
	    print "Evento desconocido: ", event

    def write_inquiry_mode(self, mode):
        """returns 0 on success, -1 on failure"""
        # save current filter
        old_filter = self.socket.getsockopt( bluez.SOL_HCI, bluez.HCI_FILTER, 14)

        # Setup socket filter to receive only events related to the
        # write_inquiry_mode command
        flt = bluez.hci_filter_new()
        opcode = bluez.cmd_opcode_pack(bluez.OGF_HOST_CTL, bluez.OCF_WRITE_INQUIRY_MODE)
        bluez.hci_filter_set_ptype(flt, bluez.HCI_EVENT_PKT)
        bluez.hci_filter_set_event(flt, bluez.EVT_CMD_COMPLETE);
        bluez.hci_filter_set_opcode(flt, opcode)
        self.socket.setsockopt( bluez.SOL_HCI, bluez.HCI_FILTER, flt )

        # send the command!
        cmd_pkt = struct.pack("B", mode)
        bluez.hci_send_cmd(self.socket, bluez.OGF_HOST_CTL, bluez.OCF_WRITE_INQUIRY_MODE, cmd_pkt)

        pkt = self.socket.recv(255)

        status = struct.unpack("xxxxxxB", pkt)[0]

        # restore old filter
        self.socket.setsockopt( bluez.SOL_HCI, bluez.HCI_FILTER, old_filter )
        if status != 0:
            return -1
        else:
            return 0
