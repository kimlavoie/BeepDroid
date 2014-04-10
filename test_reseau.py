import socket
import sys
import random
import time

mon_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

if(len(sys.argv) < 3):
    mon_socket.connect(("192.168.0.103", 6000))
else:
    mon_socket.connect((sys.argv[1],int(sys.argv[2])))

# mon_socket.send(b"<message><x>1</x></message>")
# mon_socket.send(b"<message><x>8</x></message>")
# mon_socket.send(b"<message><x>6</x></message>")


s = b""
for i in range(5000):
    s += b"<message><x>" + bytes(str(random.randint(0,10)), "utf-8") + b"</x></message>"

print(s)
mon_socket.send(s)