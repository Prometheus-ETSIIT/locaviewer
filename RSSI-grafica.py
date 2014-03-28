# -*- coding: utf-8 -*-
#
# -------------------------------------------------------------------------
# *Programa de prueba para ver el RSSI en una grafica en tiempo real*
#
# Tras recibir el paquete procedente de un dispositivo Bluetooth guarda
# el RSSI en un vector y con la ayuda de la libreria matplotlib se representa
# la grafica en tiempo real (redibujando la grafica cada vez que obtiene un dato
# nuevo).
# Tras acabar el descubrimiento de Bluetooth guarda en "potencia.png" la
# grafica.
#
# (Solo sirve cuando hay un blueetooth , si descubre otro bluetooth se añadira
# su RSSI al vector de "potenciax" por lo que la gráfica será erronea)
#
# Prometheus 2013-2014
# -------------------------------------------------------------------------

import os
import sys
import struct
import bluetooth._bluetooth as bluez # Libreria BlueZ
from matplotlib.pylab import *  # Libreria para pintar parecido a los plots de matlab


mac=sys.argv[1]
ion()
potenciay=[0]       # Vector donde se ira anadiendo la señal de potencia
datox=[0]           # Vector para indicar el numero de medida
rssim=[0]
media=0


def printpacket(pkt):
    for c in pkt:
        sys.stdout.write("%02x " % struct.unpack("B",c)[0])
    print 


def read_inquiry_mode(sock): #Modo de obtencion de lectura
    """returns the current mode, or -1 on failure"""
    # save current filter
    old_filter = sock.getsockopt( bluez.SOL_HCI, bluez.HCI_FILTER, 14)

    # Setup socket filter to receive only events related to the
    # read_inquiry_mode command
    flt = bluez.hci_filter_new()
    opcode = bluez.cmd_opcode_pack(bluez.OGF_HOST_CTL, 
            bluez.OCF_READ_INQUIRY_MODE)
    bluez.hci_filter_set_ptype(flt, bluez.HCI_EVENT_PKT)
    bluez.hci_filter_set_event(flt, bluez.EVT_CMD_COMPLETE);
    bluez.hci_filter_set_opcode(flt, opcode)
    sock.setsockopt( bluez.SOL_HCI, bluez.HCI_FILTER, flt )

    # first read the current inquiry mode.
    bluez.hci_send_cmd(sock, bluez.OGF_HOST_CTL, 
            bluez.OCF_READ_INQUIRY_MODE )

    pkt = sock.recv(255)

    status,mode = struct.unpack("xxxxxxBB", pkt)
    if status != 0: mode = -1

    # restore old filter
    sock.setsockopt( bluez.SOL_HCI, bluez.HCI_FILTER, old_filter )
    return mode

def write_inquiry_mode(sock, mode): #Modo de obteccion de escritura
    """returns 0 on success, -1 on failure"""
    # save current filter
    old_filter = sock.getsockopt( bluez.SOL_HCI, bluez.HCI_FILTER, 14)

    # Setup socket filter to receive only events related to the
    # write_inquiry_mode command
    flt = bluez.hci_filter_new()
    opcode = bluez.cmd_opcode_pack(bluez.OGF_HOST_CTL, 
            bluez.OCF_WRITE_INQUIRY_MODE)
    bluez.hci_filter_set_ptype(flt, bluez.HCI_EVENT_PKT)
    bluez.hci_filter_set_event(flt, bluez.EVT_CMD_COMPLETE);
    bluez.hci_filter_set_opcode(flt, opcode)
    sock.setsockopt( bluez.SOL_HCI, bluez.HCI_FILTER, flt )

    # send the command!
    bluez.hci_send_cmd(sock, bluez.OGF_HOST_CTL, 
            bluez.OCF_WRITE_INQUIRY_MODE, struct.pack("B", mode) )

    pkt = sock.recv(255)

    status = struct.unpack("xxxxxxB", pkt)[0]

    # restore old filter
    sock.setsockopt( bluez.SOL_HCI, bluez.HCI_FILTER, old_filter )
    if status != 0: return -1
    return 0

def device_inquiry_with_with_rssi(sock): #Obteccion del 
    # save current filter
    old_filter = sock.getsockopt( bluez.SOL_HCI, bluez.HCI_FILTER, 14)

    # perform a device inquiry on bluetooth device #0
    # The inquiry should last 8 * 1.28 = 10.24 seconds
    # before the inquiry is performed, bluez should flush its cache of
    # previously discovered devices
    flt = bluez.hci_filter_new()
    bluez.hci_filter_all_events(flt)
    bluez.hci_filter_set_ptype(flt, bluez.HCI_EVENT_PKT)
    sock.setsockopt( bluez.SOL_HCI, bluez.HCI_FILTER, flt )

    duration = 32 #4
    max_responses = 255
    cmd_pkt = struct.pack("BBBBB", 0x33, 0x8b, 0x9e, duration, max_responses)
    bluez.hci_send_cmd(sock, bluez.OGF_LINK_CTL, bluez.OCF_INQUIRY, cmd_pkt)

    results = []

    done = False

    line, = plot(datox,potenciay) #Ponemos la grafica
    axis([1,50,-90,0]) #Definimos la dimension de los ejes
    ylabel('Potencia (dBm)') #Nombre eje y
    xlabel('Iteracion') #Nombre eje x
    w=1
    while not done:
        pkt = sock.recv(255)
        ptype, event, plen = struct.unpack("BBB", pkt[:3])
        
        if event == bluez.EVT_INQUIRY_RESULT_WITH_RSSI:
            pkt = pkt[3:]
            nrsp = struct.unpack("B", pkt[0])[0]
            for i in range(nrsp):
                addr = bluez.ba2str( pkt[1+6*i:1+6*i+6] )
                rssi = struct.unpack("b", pkt[1+13*nrsp+i])[0]
                results.append( ( addr, rssi ) )
                print "[%s] RSSI: [%d]" % (addr, rssi)
                if addr== mac:
                    rssim.append(rssi)
                    potenciay.append(rssi) #Anadimos el nuevo valor de RSSI al vector potenciay
                    datox.append(w) #Anadimos el numero de iteracion al vector
                    w=w+1
                    line.set_ydata(potenciay)  # Actualizamos los datos del plot
                    line.set_xdata(datox)
                    draw() #Redibujamos la grafica
                
                

        elif event == bluez.EVT_INQUIRY_COMPLETE:
            savefig('potencia.png') #Guardamos la imagen en un archivo
            media=sum(rssim)/len(rssim)
            print(media)
            done = True
            
        elif event == bluez.EVT_CMD_STATUS:
            status, ncmd, opcode = struct.unpack("BBH", pkt[3:7])
            if status != 0:
                print "uh oh..."
                printpacket(pkt[3:7])
                done = True
        elif event == bluez.EVT_INQUIRY_RESULT:
            pkt = pkt[3:]
            nrsp = struct.unpack("B", pkt[0])[0]
            for i in range(nrsp):
                addr = bluez.ba2str( pkt[1+6*i:1+6*i+6] )
                results.append( ( addr, -1 ) )
                print "[%s] (no RRSI)" % addr
        else:
            print "unrecognized packet type 0x%02x" % ptype
	    print "event ", event


    # restore old filter
    sock.setsockopt( bluez.SOL_HCI, bluez.HCI_FILTER, old_filter )

    return results

dev_id = 0
try:
    sock = bluez.hci_open_dev(dev_id)
except:
    print "error accessing bluetooth device..."
    sys.exit(1)

try:
    mode = read_inquiry_mode(sock)
except Exception, e:
    print "error reading inquiry mode.  "
    print "Are you sure this a bluetooth 1.2 device?"
    print e
    sys.exit(1)
print "Modo de obtenccion actual %d" % mode

if mode != 1:
    print "writing inquiry mode..."
    try:
        result = write_inquiry_mode(sock, 1)
    except Exception, e:
        print "error writing inquiry mode.  Are you sure you're root?"
        print e
        sys.exit(1)
    if result != 0:
        print "error while setting inquiry mode"
    print "result: %d" % result

print "Empezamos a medir"
device_inquiry_with_with_rssi(sock)
print (media)


