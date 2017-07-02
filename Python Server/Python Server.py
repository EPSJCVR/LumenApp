import socket
import json
import threading  # libreria para usar threads
import time
import RPi.GPIO as GPIO  # Cargamos la libreria RPi.GPIO
GPIO.setmode(GPIO.BOARD)  # pines fisicos

# pinLuz =  # pin luz
pinB = 33  # pin led azul
pinG = 35  # pin led verde
pinR = 38  # pin led rojo
#pinFotoRes = 18
# pinPir =

# seteo de Luz:
GPIO.setup(pinLuz, GPIO.OUT)

# seteo de tira led:
GPIO.setup(pinR, GPIO.OUT)  # setea modo salida
GPIO.setup(pinG, GPIO.OUT)
GPIO.setup(pinB, GPIO.OUT)
red = GPIO.PWM(pinR, 100)  # establece pwm de los pines led, a 100Hz
green = GPIO.PWM(pinG, 100)
blue = GPIO.PWM(pinB, 100)
blue.start(0)  # Iniciamos el objeto 'blue' al 0% del ciclo de trabajo (completamente apagado)
green.start(0)
red.start(0)

# seteo sensor Pir:
GPIO.setup(pinPir, GPIO.IN)


# la clase switch y funcion case son solo para poder usar el switch en
# python (ya q no tiene).
# Fuente: https://stackoverflow.com/questions/60208/replacements-for-switch-statement-in-python


class switch(object):
    value = None

    def __new__(class_, value):
        class_.value = value
        return True


def case(*args):
    return any((arg == switch.value for arg in args))


def encenderLuz():
    GPIO.output(pinLuz, GPIO.HIGH)
    print("Luz escendida.")


def apagarLuz():
    GPIO.output(pinLuz, GPIO.LOW)
    print("Luz apagada.")


def apagarLed():
    red.ChangeDutyCycle(0)
    blue.ChangeDutyCycle(0)
    green.ChangeDutyCycle(0)
    print("Led apagado.")


def cambiarColorLed(r, g, b, i):
    r *= i / 100
    g *= i / 100
    b *= i / 100
    red.ChangeDutyCycle(int(r))
    blue.ChangeDutyCycle(int(g))
    green.ChangeDutyCycle(int(b))
    print("Color de led cambiado a R:", r, " G:", g, " B:", b, "Intensidad: ", i)


def leerFotoRes():
    count = 0

    # Output on the pin for
    GPIO.setup(pinFotoRes, GPIO.OUT)
    GPIO.output(pinFotoRes, GPIO.LOW)
    time.sleep(0.1)

    # Change the pin back to input
    GPIO.setup(pinFotoRes, GPIO.IN)

    # Count until the pin goes high
    while (GPIO.input(pinFotoRes) == GPIO.LOW):  # cuenta hasta que se cargar capasitor
        count += 1
    return count


def leerPir():
    value = GPIO.input(pinPir)
    return value  # devuelve 0 o 1


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


def modificarActuadores(msgJson):
    estado_actual = leerpines()
    # print(msgJson)
    if (estado_actual[0] == {"Luz": 0} and msgJson[0] == {"Luz": 1}):
        encenderLuz()
    if (estado_actual[0] == {"Luz": 1} and msgJson[0] == {"Luz": 0}):
        apagarLuz()
    if (msgJson[1] == {"Led": 1}):
        cambiarColorLed(msgJson[2], msgJson[3], msgJson[4], msgJson[5])  # encenderLed()
    if (estado_actual[1] == {"Led": 1} and msgJson[1] == {"Led": 0}):
        apagarLed()


def leerpines():
    # Json trucho para probar
    ''' msgJson = [{"Luz": 0}, {"Led": 0}, {"Red": 0}, {"Green": 88},
               {"Blue": 33}, {"Dim": 50}, {"Auto": 1}, {"Actualizar": 0}] '''
    # Leer pines
    msgJson = []  # declaro array (en python se le dice lista) vacio
    luz = GPIO.input(pinLuz)
    if(luz == 1):
        msgJson.append({"Luz": 1})  # append() agrega nuevo valor al final del array
    else:
        msgJson.append({"Luz": 0})
    # seguir con los demas pines
    # hay q ver como determinar estado de led y del automatico. (idea: flag global)
    return msgJson


'''
def leerFoto():
    return 0  # Simula fotocelular que detecta luz


def leerPir():
    return 1  # Simula movimiento
'''


def hacerAutomaticamente(e, r, g, b):
    estado_actual = leerpines()
    while(e.wait):
        if(leerFoto() == 0 and leerPir() == 1):  # No hay luz y hay movimiento
            if(estado_actual[1] == {"Led": 0}):  # led esta apagado
                encenderLed()
                estado_actual[1] = {"Led": 1}
            if(estado_actual[0] == {"Luz": 0}):  # luz esta apagada
                encenderLuz()
                estado_actual[0] = {"Luz": 1}

        if(estado_actual[0] == {"Luz": 1} and leerPir() == 0):  # luz prendida y no hay movimiento
            apagarLuz()
            estado_actual[0] = {"Luz": 0}

        if(estado_actual[1] == {"Led": 1} and leerPir() == 0):  # led prendida y no hay movimiento
            apagarLed()
            estado_actual[1] = {"Led": 0}

        time.sleep(5)  # verificar el sleep con el delay del pir, ojo!


# comienza ejecución
socketServer = socket.socket()
host = "192.168.0.11"  # "127.0.0.1"  # socket.gethostname()
port = 5053
socketServer.bind((host, port))
socketServer.listen(0)
print("Servidor creado y esperando conexiones...")

e = threading.Event()


while True:  # Falta implementar la manera de que se pueda cerrar bien el socketServer.
    clientSocket = connect(socketServer)  # me conecto con cliente
    msg = clientSocket.recv(1024).decode()  # recibo mensaje del cliente
    msgJson = json.loads(msg)  # creo el json
    print("Json recivido:", msgJson)

    # Prueba de Actualizar JSON PARA PROBAR DETECCION DE ERRORES
    if(msgJson[0] == {'Actualizar': 1}):  # Boton actualizar
        # Se chekea estado de pines para crear Json con informacion del contexto
        msgJson = leerpines()  # Obj JSON con los estados ac
    else:
        if(msgJson[6] == {'Auto': 1}):  # Modo Automatico activado
            # Crear hilo para escuchar sensores
            e.clear()  # Cierra hilos en caso de que haya alguno abierto
            t1 = threading.Thread(name="sensorListener",
                                  target=hacerAutomaticamente,
                                  args=(e, msgJson[2], msgJson[3], msgJson[4],))
            e.set()
            t1.start()
            time.sleep(0.5)
            msgJson = leerpines()
        else:  # Boton Cambiar/Shake/Voice Recognition
            # e.clear()  # Frenar hilo
            modificarActuadores(msgJson)  # Modificar actuadores
            # msgJson = leerpines()

    # MODIFICO JSON PARA PROBAR DETECCION DE ERRORES
    # msgJson[0] = {'Luz': 0}  # Luz
    # msgJson[1]  = {'Led': 0} #Led
    #  print("Json modificado y enviado, para probar errores")
    # print(msgJson) #json modificado

    # code = msgJson["Code"]  # utilizo el json y busco en él
    # runFunction(code)  # realizo la tarea solicitada por el cliente
    print("Mensaje a enviar:", msgJson)
    msgSend = json.dumps(msgJson)
    clientSocket.send(msgSend.encode())
    clientSocket.close()

socketServer.close()
blue.stop()            # Detenemos el objeto 'blue'
green.stop()
red.stop()             # Detenemos el objeto 'green'
GPIO.cleanup()
print("Hasta luego loquita")


'''
Cosas que faltan del server:
-poder cerrar bien el servidor (poder salir del while(true) y que se cierren todos los pines y sockets)
-leerpines(): determinar como saber el estado del led y del automatico.
-cambiar hacerAutomaticamente(), lo de encerderLed
'''
