package com.cansnifferfx.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import jssc.*;
import java.io.IOException;
import java.util.ArrayList;

public class SampleController {
    public TextField outcomingPacket;
    public TextField incomingPacket;
    public Button sendButton;
    public TextField desktopId;
    public TextField desktopDataSize;
    public TextField desktopData;
    public TextField desktopCRC;
    public Button recieveButton;
    public TextField deviceId;
    public TextField deviceDataSize;
    public TextField deviceData;
    public TextField deviceCRC;
    public ListView recievedPacketsList;
    public ListView sendedPacketsList;
    public ComboBox portNumberCB;
    public ComboBox stopbitCB;
    public TextField intervalBetweenByteRecieve;
    public CheckBox autoAnswerCheckBox;
    public ComboBox speedCB;
    public ComboBox parityCB;
    public TextField intervalBetweenByteSend;
    public TextField period;
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
        speedCB.setValue(comSpeedsObserList.get(0));

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

    public void exitAction(ActionEvent actionEvent) {

    }

    public void deleteAction(ActionEvent actionEvent) {

    }

    public void aboutAction(ActionEvent actionEvent){

    }

    public void sendButtonAction(ActionEvent actionEvent) {

    }

    public void recieveButtonAction(ActionEvent actionEvent) {

    }

    public void autoAnswerAction(ActionEvent actionEvent) {

    }
}
