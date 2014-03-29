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

if __name__ == "__main__":
    # Lee los argumentos necesarios
    parser = argparse.ArgumentParser(
        description = "Estudia la potencia recibida de un dispositivo Bluetooth")
    parser.add_argument("MAC", help = "Dirección HW del dispositivo destino.")
    parser.add_argument("salida", help = "Archivo donde se guardará la gráfica.")
    parser.add_argument("-d", nargs = 2, default = [50, 200],
                        help = "Rango de distancias (cm)")
    parser.add_argument("-dstep", default = 20, help = "Incremento distancia (cm)")
    parser.add_argument("-a", nargs = 2, default = [90, 90], 
                        help = "Rango de ángulos (grados)")
    parser.add_argument("-astep", default = 10, help = "Incremento ángulos (grados)")
    args = parser.parse_args()
    print args

    inquirier = BluezInquiry(0, args.MAC)
    signals = inquirier.inquiry()
    while inquirier.is_inquiring():
        inquirier.process_event()
    
    # TODO: Obtener valor medio de potecia leída
    # TODO: Repetir proceso para cada rango de distancia y ángulo
    # TODO: Generar gráfica final.
