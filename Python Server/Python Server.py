import socket
import json

# la clase switch y funcion case son solo para poder usar el switch en
# python (ya q no tiene).
# Fuente: https://stackoverflow.com/questions/60208/replacements-for-switch-statement-in-python

global dia, mes, año, hora, minuto

global alarma_status
alarma_status = 0


class switch(object):
    value = None

    def __new__(class_, value):
        class_.value = value
        return True


def case(*args):
    return any((arg == switch.value for arg in args))


def encenderLuz():
    print("Luz escendida.")


def apagarLuz():
    print("Luz apagada.")


def runFunction(code):  # se determina a que funcion llamar
    switch(code)
    if case(0):
        encenderLuz()
    elif case(1):
        apagarLuz()


def connect(socketServer):  # se conecta con un cliente y devuelve su socket
    c, addr = socketServer.accept()
    print("Conexion recibida de: ", addr)
    # c.send("Bienvenido cliente".encode())
    return c


def leerpines():
    # Json trucho para probar
    msgJson = [{"Luz": 1}, {"Led": 0}, {"Red": 0}, {"Green": 88},
               {"Blue": 33}, {"Dim": 50}, {"Auto": 1}, {"Actualizar": 0}]
    # Leer pines
    return msgJson


def establecerAlarma(msgJson):
    global dia, mes, año, hora, minuto
    if(msgJson[0] == {'Alarma': 1}):
        dia = msgJson[1]["Día"]
        mes = msgJson[2]["Mes"]
        año = msgJson[3]["Año"]
        hora = msgJson[4]["Hora"]
        minuto = msgJson[5]["Minuto"]
    if(msgJson[0] == {'Alarma': 0}):
        dia = 0
        mes = 0
        año = 0
        hora = 0
        minuto = 0


socketServer = socket.socket()
host = "192.168.0.14"  # "127.0.0.1"  # socket.gethostname()
port = 5052
socketServer.bind((host, port))
socketServer.listen(0)
print("Servidor creado y esperando conexiones...")

try:
    while True:
        clientSocket = connect(socketServer)  # me conecto con cliente
        msg = clientSocket.recv(1024).decode()  # recibo mensaje del cliente
        msgJson = json.loads(msg)  # creo el json
        print("Json recivido:", msgJson)

        # Prueba de Actualizar JSON PARA PROBAR DETECCION DE ERRORES
        if(msgJson[0] == {'Actualizar': 1}):
            # Se chekea estado de pines para crear Json con informacion del contexto
            if(alarma_status == 1):
                msgJsonPines = leerpines()  # Obj JSON con los estados ac
                msgJsonPines.append({'Alarma': 1})
                alarmaJson = []
                alarmaJson.append({'Día': dia})
                alarmaJson.append({'Mes': mes})
                alarmaJson.append({'Año': año})
                alarmaJson.append({'Hora': hora})
                alarmaJson.append({'Minuto': minuto})
                msgJsonPines.append(alarmaJson)
                msgJson = json.loads(json.dumps(msgJsonPines))
            else:
                msgJson = leerpines()
                msgJson.append({'Alarma': 0})
        if(msgJson[0] == {'Alarma': 1}):
            establecerAlarma(msgJson)  # crea thread que se ocupa de la alarma
            msgJsonPines = leerpines()
            msgJsonPines.append({"Alarma": 1})
            alarmaJson = []
            alarmaJson.append(msgJson[1])  # dia
            alarmaJson.append(msgJson[2])  # mes
            alarmaJson.append(msgJson[3])  # año
            alarmaJson.append(msgJson[4])  # hora
            alarmaJson.append(msgJson[5])  # minuto
            msgJsonPines.append(alarmaJson)
            msgJson = json.loads(json.dumps(msgJsonPines))
            alarma_status = 1
        if(msgJson[0] == {'Alarma': 0}):
            establecerAlarma(msgJson)  # elimina thread que se ocupa de la alarma
            msgJson = leerpines()
            msgJson.append({"Alarma": 0})
            alarma_status = 0
            # MODIFICO JSON PARA PROBAR DETECCION DE ERRORES
            # msgJson[0] = {'Luz': 0}  # Luz
            # msgJson[1]  = {'Led': 0} #Led
            #print("Json modificado y enviado, para probar errores")
            # print(msgJson) #json modificado

            # code = msgJson["Code"]  # utilizo el json y busco en él
            # runFunction(code)  # realizo la tarea solicitada por el cliente
        print("Mensaje a enviar:", msgJson)
        msgSend = json.dumps(msgJson)
        clientSocket.send(msgSend.encode())
        clientSocket.close()

except KeyboardInterrupt:
    pass
socketServer.close()
clientSocket.close()
print("Hasta luego locquita")
