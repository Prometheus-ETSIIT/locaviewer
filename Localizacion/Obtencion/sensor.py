#!/bin/python
# -*- coding: utf-8 -*-
#
# Se le indica el ID del bluetooth y el puerto de envío.
# Saca el RSSI y lo envia.

from BluezInquiry import BluezInquiry
import sys
from subprocess import Popen, PIPE


# Inquiry de forma infinita
def inquiry(inquirier):
    while True:
        inquirier.inquiry()
        while inquirier.is_inquiring():
            inquirier.process_event()


# Obtenemos la ID y el puerto por el que se enviaran los datos
dev_id = int(sys.argv[1])
port = int(sys.argv[2])

# Obtenemos la MAC del dispositivo a partir del ID
mac = None
hci_out = Popen(['hcitool', 'dev'], stdout=PIPE).stdout.readlines()
del hci_out[0]  # 'Devices:\n'

# Por cada dispositivo
for dev in hci_out:
    opts = dev[1:-1].split('\t')  # Elimino el primer tabulador y \n y divido
    if opts[0] == "hci" + str(dev_id):
        mac = opts[1]
print("Soy " + mac)

# Comienza el inquiry
inquirier = BluezInquiry(dev_id, mac, port)
inquiry(inquirier)
