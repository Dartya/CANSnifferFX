package com.cansnifferfx.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import jssc.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class SampleController {
    //FXML Views
    public TextField outcomingPacket;
    public TextField incomingPacket;
    public Button sendButton;
    public TextField desktopId;
    public TextField desktopDataSize;
    public TextField desktopData;
    public Button recieveButton;
    public TextField deviceId;
    public TextField deviceDataSize;
    public TextField deviceData;
    public ListView recievedPacketsList;
    public ListView sendedPacketsList;
    public ComboBox portNumberCB;
    public ComboBox stopbitCB;
    public CheckBox autoAnswerCheckBox;
    public ComboBox speedCB;
    public ComboBox parityCB;
    public static SerialPort serialPort;

    //ArrayLists
    private ArrayList<Integer> baudRates = new ArrayList<Integer>();
    private ArrayList<Integer> stopBits = new ArrayList<Integer>();
    private ArrayList<Integer> parity = new ArrayList<Integer>();

    //ObservableArrayLists
    private ObservableList<String> comPortsObserList = FXCollections.observableArrayList();
    private ObservableList<Integer> comSpeedsObserList = FXCollections.observableArrayList();
    private ObservableList<Integer> comStopBitsObserList = FXCollections.observableArrayList();
    private ObservableList<Integer> comParityObserList = FXCollections.observableArrayList();

    //строки
    private static String incomingString;

    public void initialize() {
        // getting serial ports list into the array
        String[] portNames = SerialPortList.getPortNames();

        if (portNames.length == 0) {
            System.out.println("There are no serial-ports :( You can use an emulator, such ad VSPE, to create a virtual serial port.");
            System.out.println("Press Enter to exit...");
            try {
                System.in.read();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return;
        }

        for (int i = 0; i < portNames.length; i++) {
            System.out.println(portNames[i]);
        }
        //инициализация комбобокса выбора портов
        comPortsObserList.setAll(portNames);
        portNumberCB.setItems(comPortsObserList);
        portNumberCB.setValue(comPortsObserList.get(0));

        //инициализация комбобокса выбора скорости
        initComBaudRates();
        comSpeedsObserList.setAll(baudRates);
        speedCB.setItems(comSpeedsObserList);
        speedCB.setValue(115200);

        //инициализация комбобокса стоп-битов
        initComStopBits();
        comStopBitsObserList.setAll(stopBits);
        stopbitCB.setItems(comStopBitsObserList);
        stopbitCB.setValue(comStopBitsObserList.get(0));

        //инициализация комбобокса паритета
        initComParity();
        comParityObserList.setAll(parity);
        parityCB.setItems(comParityObserList);
        parityCB.setValue(comParityObserList.get(0));

        //первичная инициализация серийного порта
        initSerialPort();
    }

    private void initComBaudRates(){   //метод инициализации листа скоростей настраиваемого порта
        baudRates.add(SerialPort.BAUDRATE_9600);
        baudRates.add(SerialPort.BAUDRATE_19200);
        baudRates.add(SerialPort.BAUDRATE_38400);
        baudRates.add(SerialPort.BAUDRATE_57600);
        baudRates.add(SerialPort.BAUDRATE_115200);
        baudRates.add(SerialPort.BAUDRATE_128000);
        baudRates.add(SerialPort.BAUDRATE_256000);
    }

    private void initComStopBits(){
        stopBits.add(SerialPort.STOPBITS_1);
        stopBits.add(SerialPort.STOPBITS_2);
    }

    private void initComParity(){
        parity.add(SerialPort.PARITY_NONE);
        parity.add(SerialPort.PARITY_EVEN);
    }

    private void initSerialPort(){
        serialPort = new SerialPort((String)portNumberCB.getValue());

        try{
            //Открываем порт
            serialPort.openPort();
            //Выставляем параметры порта
            serialPort.setParams(SerialPort.BAUDRATE_115200,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            //Включаем аппаратное управление потоком
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

            //Устанавливаем ивент листенер и маску
            serialPort.addEventListener(new PortReader(), SerialPort.MASK_RXCHAR);
        } catch (SerialPortException exc){
            System.out.println("Ошибка инициализации порта! "+ exc);
        }
    }

    private void sendMessage(String message){
        System.out.println("Попытка отправки сообщения \""+message+"\":");
        message = message + (char)13;   //(char)13 = 0Dh - возврат каретки в ASCII
        try{
            printStringHexCodes(message);
            boolean isSucceed = serialPort.writeString(message);
            if (isSucceed == true)
                System.out.println("Отправка успешна.");
            else
                System.out.println("Отправка не удалась.");
        } catch (Exception exc){
            System.out.println("Ошибка передачи сообщения! "+ exc);
        }
    }

    public void getMessage(String message){
        incomingPacket.setText(message);
    }

    public void printStringHexCodes(String message){
        byte[] buffer = message.getBytes();
        System.out.print("Массив байт: ");
        for (int i = 0; i < buffer.length; i++) {
            if (i == buffer.length-1)
                System.out.println(buffer[i]);
            else
                System.out.print(buffer[i]+" ");
        }
    }

    public void exitAction(ActionEvent actionEvent) {

    }

    public void deleteAction(ActionEvent actionEvent) {

    }

    public void aboutAction(ActionEvent actionEvent){

    }

    public void sendButtonAction(ActionEvent actionEvent) {
        sendMessage(outcomingPacket.getText());
    }

    public void recieveButtonAction(ActionEvent actionEvent) {
        getMessage(incomingString);
    }

    public void autoAnswerAction(ActionEvent actionEvent) {

    }

    private static class PortReader implements SerialPortEventListener {
        byte[] buffer;
        public void serialEvent(SerialPortEvent event) {
            if (event.isRXCHAR() && (event.getEventValue() >= 6 && event.getEventValue() <= 22)){

                try {
                    buffer = getData();
                }catch (Exception e){
                    e.printStackTrace();
                }
                System.out.print("Полученный массив байт: ");
                for (int i = 0; i < buffer.length; i++) {
                    if (buffer[i] != 13)
                        System.out.print(buffer[i]+" ");
                    else System.out.println(buffer[i]);
                }
            }
        }
        byte[] getData() throws SerialPortException, IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] b;

            try {
                while ((b = serialPort.readBytes(1, 100)) != null) {
                    baos.write(b);
                    //System.out.println ("Wrote: " + b.length + " bytes");
                }
                //System.out.println("Returning: " + Arrays.toString(baos.toByteArray()));
            } catch (SerialPortTimeoutException ex) {
                //не нужно отлавливать эту ошибку - она просто означает, что нет данных для чтения. Не нужно срать в терминал по такм пустякам.
            }
            return baos.toByteArray();
        }
    }
}