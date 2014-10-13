#!/bin/python
# -*- coding: utf-8 -*-
#
# Se le indica el ID del bluetooth y el puerto de envío.
# Saca el RSSI y lo envia.

import BluezInquiry
import sys


def inquiry(inquirier):
    inquirier.inquiry()
    while inquirier.is_inquiring():
        inquirier.process_event()

    return

# Obtenemos la ID y el puerto por el que se enviaran los datos
ID_blue = int(sys.argv[1])
Port = int(sys.argv[2])

inquirier = BluezInquiry(ID_blue, Port)
inquiry(inquirier)
