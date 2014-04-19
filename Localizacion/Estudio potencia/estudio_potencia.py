# -*- coding: utf-8 -*-
#
###############################################
# Script para realizar un estudio de potencia #
# de un dispositivo Bluetooth.                #
#                                             #
# V 1.0: Benito Palacios Sánchez              #
#   - Recorre rango de distancia y ángulo y   #
#     y genera una gráfica.                   #
#                                             #
# Copyright Prometheus 2014                   #
###############################################

import argparse
from BluezInquiry import *
from mayavi import mlab
import matplotlib.pyplot as plt
import numpy as np

MIN_RSSI = -90
MAX_RSSI = -20
DUTYCICLES = 2

def inquiry(inquirier):
    inquirier.inquiry()
    while inquirier.is_inquiring():
        inquirier.process_event()
    
    return inquirier.get_mean()

def inquiry_times(inquirier, times):
    inquirier.clear_samples()
    for i in range(times):
        inquiry(inquirier)
    return inquirier.get_mean()

if __name__ == "__main__":
    # Lee los argumentos necesarios
    parser = argparse.ArgumentParser(
        description = "Estudia la potencia recibida de un dispositivo Bluetooth")
    parser.add_argument("MAC", help = "Dirección HW del dispositivo destino.")
    parser.add_argument("salida", help = "Archivo donde se guardará la gráfica.")
    parser.add_argument("-d", nargs = 2, default = ["50", "200"],
                        help = "Rango de distancias (cm)")
    parser.add_argument("-dstep", default = "20", help = "Incremento distancia (cm)")
    parser.add_argument("-a", nargs = 2, default = ["90", "91"], 
                        help = "Rango de ángulos (grados)")
    parser.add_argument("-astep", default = "10", help = "Incremento ángulos (grados)")
    args = parser.parse_args()
    print args
    
    # Rangos de operación
    dmin  = int(args.d[0])
    dmax  = int(args.d[1])
    dstep = int(args.dstep)
    amin  = int(args.a[0])
    amax  = int(args.a[1])
    astep = int(args.astep)
    
    angles    = range(amin, amax + 1, astep)
    distances = range(dmin, dmax + 1, dstep)
    
    # Variables para guardar los datos
    medidas_ang = { }  # Diccionario: angulo => [(distancias), (valores)]
    medidas_dis = { }  # Diccionario: distancia => [(angulos), (valores)]
    x = [ 0.0 ]        # Coordenada X para mapa temperatura
    y = [ 0.0 ]        # Coordenada Y para mapa temperatura
    z = [ 0 ]          # Valor para mapa temperatura
    
    # Variable para pedir los datos
    inquirier = BluezInquiry(0, args.MAC)
    
    # Por cada ángulo...
    salir = False
    for angle in angles:
        angle_rad = angle / 180.0 * np.pi
        if salir:   # Si se ha solicitado salir de la obtención de datos
            break
    
        # Para cada distancia...
        print "# A %.1f grados" % angle
        for dist in distances:
            print "\t# A %.1f centímetros  " % dist,
            
            # Espera a que el usuario se coloque
            entrada = raw_input(".")
            if entrada == "q":  # Compueba que no quiera abortar
                salir = True
                break
                        
            # Obtiene el valor
            valor = inquiry_times(inquirier, DUTYCICLES)
            print "\t\t%d" % valor
            
            # Lo guarda en las variables           
            z.append(valor)
            x.append(np.cos(angle_rad)*dist)
            y.append(np.sin(angle_rad)*dist)
            
            if not medidas_ang.has_key(angle):
                medidas_ang[angle] = [(), ()]
            medidas_ang[angle][0] += (dist,)
            medidas_ang[angle][1] += (valor,)
            
            if not medidas_dis.has_key(dist):
                medidas_dis[dist] = [(), ()]
            medidas_dis[dist][0] += (angle,)
            medidas_dis[dist][1] += (valor,)
     
    # Guardo los datos en un fichero para recuperarlo más tarde
    filename = args.salida + ".txt"
    with open(filename, "w") as f:
        f.write("# x y\n")
        np.savetxt(f, np.array([x, y, z]).T)
    
    # Genero muuuchas gráficas
    # 1º Para cada ángulo según la distancia
    for angle in medidas_ang:
        plt.figure()
        plt.plot(medidas_ang[angle][0], medidas_ang[angle][1], 'r')
        plt.xlabel("Distancia (cm)")
        plt.ylabel("RSSI (dB)")
        plt.title("RSSI segun distancia para %.1f grados" % angle)
        plt.axis([ dmin, dmax, MIN_RSSI, MAX_RSSI ])
        plt.savefig(filename + "angle_" + str(angle) + ".png")
        plt.show()
        
    # 2º Para cada distancia según el ángulo
    for dis in medidas_dis:
        plt.figure()
        plt.plot(medidas_dis[dis][0], medidas_dis[dis][1], 'b')
        plt.xlabel("Angulo (grados)")
        plt.ylabel("RSSI (dB)")
        plt.title("RSSI segun angulo para %d (cm)" % dis)
        plt.axis([ amin, amax, MIN_RSSI, MAX_RSSI ])
        plt.savefig(filename + "dista_" + str(dis) + ".png")
        plt.show()
    
    # Mapa de temperatura. Por Gael Varoquaux, bajo licencia BSD Style.
    # http://docs.enthought.com/mayavi/mayavi/auto/example_surface_from_irregular_data.html#example-surface-from-irregular-data
    mlab.figure(1, fgcolor=(0, 0, 0), bgcolor=(1, 1, 1))
    zero_z = np.multiply(np.arange(len(x)), 0)  # Sin coordenada Z
    pts = mlab.points3d(x, y, zero_z, z, scale_mode='none', scale_factor=3)
    mesh = mlab.pipeline.delaunay2d(pts)
    surf = mlab.pipeline.surface(mesh)
    mlab.view(0.0, 0.0, 198.2, ( 24.9, 26.5, -32.5))
    mlab.savefig(args.salida + "_temp.png")
    mlab.show()
