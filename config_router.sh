#!/bin/bash
# El primer argumento es la IP de la interfaz que tiene conexión a Internet

# Crea la ruta hacia la subred
sudo route add -net 192.168.3.0/24 gw 10.42.0.29 p35p1

# Habilita SNAT para la segunda subred
sudo iptables -t nat -I POSTROUTING -s 192.168.3.0/24 -o wlp4s0 -j SNAT --to $1

# Permite los paquetes de destino la segunda subred
sudo iptables -t filter -I FORWARD -d 192.168.3.0/24 -j ACCEPT

# Permite los paquetes con origen la segunda subred
sudo iptables -t filter -I FORWARD -s 192.168.3.0/24 -j ACCEPT

# Permite sin restricción los paquetes con destino la primera subred
sudo iptables -t filter -I FORWARD -d 10.42.0.0/24 -j ACCEPT
