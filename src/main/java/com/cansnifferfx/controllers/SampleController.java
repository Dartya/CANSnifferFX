package com.cansnifferfx.controllers;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jssc.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

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
    public MenuItem ASCIITableWin;
    public Button generateMessageButton;
    public Button copyMessageButton;
    public TextField generatedTextTextField ;

    //Some objects
    public static SerialPort serialPort;
    private static ArrayList<SerialPort> serialPorts = new ArrayList<SerialPort>();
    public ListView consoleListView;

    //ArrayLists
    private ArrayList<Integer> baudRates = new ArrayList<Integer>();
    private ArrayList<Integer> stopBits = new ArrayList<Integer>();
    private ArrayList<Integer> parity = new ArrayList<Integer>();

    //ObservableArrayLists
    private ObservableList<String> comPortsObserList = FXCollections.observableArrayList();
    private ObservableList<Integer> comSpeedsObserList = FXCollections.observableArrayList();
    private ObservableList<Integer> comStopBitsObserList = FXCollections.observableArrayList();
    private ObservableList<Integer> comParityObserList = FXCollections.observableArrayList();
    private ObservableList<String> incomingMessages = FXCollections.observableArrayList();
    private ObservableList<String> outgoingMessages = FXCollections.observableArrayList();
    private ObservableList<String> consoleMessages = FXCollections.observableArrayList();

    //строки
    private static String incomingString;
    private static String savedIncomingString = "";

    //счетчики
    private static int countMessages;
    private static int callsOfGetMessageMethod;

    //флаги
    private static boolean autoGetMessageFlag = false;

    public void initialize() {

        //получаем список всех доступных на момент запуска проги com-портов
        final String[] portNames = SerialPortList.getPortNames();

        //проверка на факт отсутствия доступных ком-портов
        if (portNames.length == 0) {
            System.out.println("Не найдено ни одного доступного ком-порта.");
            printInConsole("Не найдено ни одного доступного ком-порта.");
            System.out.println("Нажмите Enter для выхода...");
            printInConsole("Нажмите Enter для выхода...");
            try {
                System.in.read();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return;
        }

        //создание списка ком-портов с соответствующим именем
        for (int i = 0; i < portNames.length; i++) {
            serialPorts.add(new SerialPort(portNames[i]));
        }

        //вывод доступных ком-портов в консоль для отладки
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
        initSerialPort(serialPorts.get(0));
        //запоминаем текущий serialPort
        serialPort = serialPorts.get(0);

        //инициализация листов сообщений
        recievedPacketsList.setItems(incomingMessages);
        recievedPacketsList.setFixedCellSize(25);
        sendedPacketsList.setItems(outgoingMessages);
        sendedPacketsList.setFixedCellSize(25);
        //инициализация консоли
        consoleListView.setItems(consoleMessages);
        consoleListView.setFixedCellSize(25);

        //подключаем листенер чекбаттона автозаполнения
        autoGetMessageFlag = autoAnswerCheckBox.isSelected();
        autoAnswerCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                autoGetMessageFlag = newValue; //присваиваем переменной новое значение чекбаттона
                if (oldValue == true && newValue == false)
                    callsOfGetMessageMethod = countMessages;
            }
        });

        //слушатель изменения ком-порта
        portNumberCB.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                System.out.println("Старый порт: "+portNames[(Integer)oldValue]);
                printInConsole("Старый порт: "+portNames[(Integer)oldValue]);
                System.out.println("Новый порт: "+portNames[(Integer)newValue]);
                printInConsole("Новый порт: "+portNames[(Integer)newValue]);

                //закрываем старый порт и удаляем его листенер
                shutdownPort(serialPorts.get((Integer)oldValue));
                //инициализируем новый порт и его листенер
                initSerialPort(serialPorts.get((Integer)newValue));
                //сохраняем текущий порт в переменную
                serialPort = serialPorts.get((Integer)newValue);
            }
        });

        //слушатели изменения параметров комбобоксов
        //скорость
        speedCB.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                //updatePortParams();
                updatePortParams(serialPort);
            }
        });
        //паритет
        parityCB.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                updatePortParams();
            }
        });
        //стоп-бит
        stopbitCB.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                updatePortParams();
            }
        });
    }

    private void printInConsole(String message){
        Date date = new Date();
        message = date.toString()+": "+message;
        consoleMessages.add(message);
        consoleListView.scrollTo(consoleMessages.size());
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

    private void initSerialPort(SerialPort serialPort){
        //serialPort = new SerialPort((String)portNumberCB.getValue());

        try{
            //Открываем порт
            serialPort.openPort();
            //Выставляем параметры порта
            setPortParams(serialPort);
            //Включаем аппаратное управление потоком
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

            //Устанавливаем ивент листенер и маску
            serialPort.addEventListener(new PortReader(this), SerialPort.MASK_RXCHAR);
        } catch (SerialPortException exc){
            System.out.println("Ошибка инициализации порта! "+ exc);
            printInConsole("Ошибка инициализации порта! "+ exc);
        }
    }

    private void setPortParams(SerialPort serialPort){
        try{
            serialPort.setParams((Integer) speedCB.getValue(),
                    SerialPort.DATABITS_8,
                    (Integer)stopbitCB.getValue(),
                    (Integer)parityCB.getValue());

        }catch(Exception exc){
            System.out.println("Не удалось применить новые настройки ком-порта! "+ exc);
            printInConsole("Не удалось применить новые настройки ком-порта! "+ exc);
        }
    }
    //метод обновления параметров ком-порта, первая реализация - без параметра
    private void updatePortParams(){
        //получение текущего порта
        SerialPort serialPort = (SerialPort)portNumberCB.getValue();
        //установка новых параметров
        setPortParams(serialPort);
    }
    //вторая реализация - с параметром, планируется передавать текущий serialPort
    private void updatePortParams(SerialPort serialPort){
        //установка новых параметров
        setPortParams(serialPort);
    }

    private void shutdownPort(SerialPort serialPort){
        try {
            serialPort.closePort();
            serialPort.removeEventListener();
        }catch(SerialPortException exc){
            System.out.println("Ошибка закрытия порта! "+ exc);
            printInConsole("Ошибка закрытия порта! "+ exc);
        }
    }

    private void sendMessage(String message){
        System.out.println("Попытка отправки сообщения \""+message+"\":");
        printInConsole("Попытка отправки сообщения \""+message+"\":");
        message = message + (char)13;   //(char)13 = 0Dh - возврат каретки в ASCII
        try{
            printStringHexCodes(message);
            boolean isSucceed = serialPort.writeString(message);
            if (isSucceed == true) {
                System.out.println("Отправка успешна.");
                printInConsole("Отправка успешна");
            }
            else {
                System.out.println("Отправка не удалась.");
                printInConsole("Отправка не удалась.");
            }
        } catch (Exception exc){
            System.out.println("Ошибка передачи сообщения! "+ exc);
            printInConsole("Ошибка передачи сообщения! "+ exc);
        }
        //работаем с вьюхами окна:
        String strbuf = "";
        byte[] buffer = message.getBytes();
        if ((desktopId.getText().equals("") || desktopDataSize.getText().equals("") || desktopData.getText().equals("")) && !outcomingPacket.getText().equals("") ){
            //устанавливаем байты идентификатора
            for (int i = 1; i < 4; i++) {
                strbuf = strbuf + (buffer[i]) + " ";
            }
            desktopId.setText(strbuf);
            strbuf = "";

            //устанавливаем длину пакета
            desktopDataSize.setText(String.valueOf((char) buffer[4]));

            //устанавливаем байты данных
            for (int i = 5; i < buffer.length - 1; i++) {
                strbuf = strbuf + (buffer[i]) + " ";
            }
            desktopData.setText(strbuf);
        }

        //автоматическая прокрутка до последнего сообщения
        outgoingMessages.add(message);
        sendedPacketsList.scrollTo(outgoingMessages.size());
    }

    public void getMessage(String message){
        if ((savedIncomingString.equals(incomingString)) && (countMessages == callsOfGetMessageMethod))
            return;
        else {
            incomingPacket.setText(message);
            byte[] buffer = message.getBytes();
            String strbuf = "";
            //устанавливаем байты идентификатора
            for (int i = 1; i < 4; i++) {
                strbuf = strbuf + (buffer[i]) + " ";
            }
            deviceId.setText(strbuf);
            strbuf = "";

            //устанавливаем длину пакета
            deviceDataSize.setText(String.valueOf((char) buffer[4]));

            //устанавливаем байты данных
            for (int i = 5; i < buffer.length - 1; i++) {
                strbuf = strbuf + (buffer[i]) + " ";
            }
            deviceData.setText(strbuf);

            //обновляем обсервабллист
            incomingMessages.add(incomingString);
            recievedPacketsList.scrollTo(incomingMessages.size());

            callsOfGetMessageMethod++;
            //автоматическая прокрутка до последнего сообщения
            savedIncomingString = incomingString;
        }
    }

    public void printStringHexCodes(String message){
        byte[] buffer = message.getBytes();
        System.out.print("Массив байт: ");
        String mes = "Массив байт: ";
        for (int i = 0; i < buffer.length; i++) {
            if (i == buffer.length-1) {
                System.out.println(buffer[i]);
                mes = mes+buffer[i];
            }
            else {
                System.out.print(buffer[i] + " ");
                mes = mes+buffer[i];
            }
        }
        printInConsole(mes);
    }

    public void exitAction(ActionEvent actionEvent) {

    }

    public void deleteAction(ActionEvent actionEvent) {
        sendedPacketsList.getItems().clear();
        recievedPacketsList.getItems().clear();
        consoleListView.getItems().clear();
    }

    public void aboutAction(ActionEvent actionEvent){

    }

    public void sendButtonAction(ActionEvent actionEvent) {
        if (!outcomingPacket.getText().equals("") || outcomingPacket.getText() == null)
            sendMessage(outcomingPacket.getText());
    }

    public void recieveButtonAction(ActionEvent actionEvent) {
        if (countMessages != 0 && !autoGetMessageFlag)
            getMessage(incomingString);
    }

    public void autoAnswerAction(ActionEvent actionEvent) {

    }

    public void openASCIITableWinAction(ActionEvent actionEvent) {
        try {
            Stage aboutstage = new Stage();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/asciiWin.fxml"));
            Parent root = loader.load();
            aboutstage.setTitle("Таблица символов ASCII");
            aboutstage.setMinHeight(500);
            aboutstage.setMinWidth(700);
            aboutstage.setResizable(false);
            aboutstage.setScene(new Scene(root));
            aboutstage.initModality(Modality.APPLICATION_MODAL);
            //stage.initOwner(((Node)actionEvent.getSource()).getScene().getWindow());    //указывается родительское окно
            //правда, данный метод инициализации родительского окна не работает с элеменами основного меню, поэтому
            aboutstage.show();         //не используется в связке с stage.initModality(Modality.WINDOW_MODAL);
            //aboutstage.showAndWait();    //зато используется этот метод в связке с stage.initModality(Modality.APPLICATION_MODAL);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generateMessageAction(ActionEvent actionEvent) {
        if (!(desktopId.getText().equals("") || desktopDataSize.getText().equals("") || desktopData.getText().equals("")))
        {
            String strbuf = "";
            String strresult = "t";
            byte[] buffer1;
            byte[] buffer2;
            //считываем идентификатор
            buffer1 = desktopId.getText().getBytes();
            for (int i = 0; i < buffer1.length; i++) {
                strbuf = strbuf+(char)buffer1[i];
            }
            strbuf.replaceAll("\\s","");
            strresult = strresult+strbuf;

            //считываем длину пакета
            strbuf = desktopDataSize.getText();
            strbuf.replaceAll("\\s","");
            strresult = strresult+strbuf;
            strbuf = "";

            //считываем пакет данных
            buffer2 = desktopData.getText().getBytes();
            for (int i = 0; i < buffer2.length; i++) {
                strbuf = strbuf+(char)buffer2[i];
            }
            strbuf.replaceAll("\\s","");
            strresult = strresult+strbuf;

            generatedTextTextField.setText(strresult);
        }
    }

    public void copyMessageAction(ActionEvent actionEvent) {
        outcomingPacket.setText(generatedTextTextField.getText());
    }

    private class PortReader implements SerialPortEventListener {
        byte[] buffer;

        SampleController sampleController;

        PortReader(SampleController sampleController){
            this.sampleController = sampleController;
        }

        public void serialEvent(SerialPortEvent event) {
            if (event.isRXCHAR() && (event.getEventValue() >= 6 && event.getEventValue() <= 22)){
                try {
                    buffer = getData();
                }catch (Exception e){
                    e.printStackTrace();
                }
                System.out.print("Полученный массив байт: ");
                String message = ("Полученный массив байт по ком-порту "+event.getPortName()+": ");
                for (int i = 0; i < buffer.length; i++) {
                    if (buffer[i] != 13) {
                        System.out.print(buffer[i] + " ");
                        message = message+(int)buffer[i] + " ";
                    }
                    else {
                        System.out.println(buffer[i]);
                        message = message+(int)buffer[i] + " ";
                    }
                }
                incomingString = "";
                for (int i = 0; i < buffer.length; i++) {
                    incomingString = incomingString+(char)buffer[i];
                }
                countMessages++;
                //The user interface cannot be directly updated from a non-application thread. Instead, use Platform.runLater(), with the logic inside the Runnable object. For example:
                //https://stackoverflow.com/questions/17850191/why-am-i-getting-java-lang-illegalstateexception-not-on-fx-application-thread
                if (autoGetMessageFlag) {
                    Platform.runLater(new Runnable() {
                        public void run() {
                            sampleController.getMessage(incomingString);
                        }
                    });
                }

                final String finMessage = message;
                Platform.runLater(new Runnable() {
                    public void run() {
                        printInConsole(finMessage);
                    }
                });
            }
        }
        private byte[] getData() throws SerialPortException, IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] b;

            try {
                while ((b = serialPort.readBytes(1, 100)) != null) {
                    baos.write(b);
                    //System.out.println ("Wrote: " + b.length + " bytes");
                }
                //System.out.println("Returning: " + Arrays.toString(baos.toByteArray()));
            } catch (SerialPortTimeoutException ex) {
                //не нужно отлавливать эту ошибку - она просто означает, что нет данных для чтения. Не нужно срать в терминал по таким пустякам.
            }
            return baos.toByteArray();
        }
    }
}