package com.cansnifferfx.controllers;

import com.cansnifferfx.models.AutoSendFrameThread;
import com.cansnifferfx.models.Messages;
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
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jssc.*;

import java.io.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    //objects from new tabs
    public TextField desctopDataComand;
    public TextField desctopDataValue;
    public TextField desctopDataIndex;
    public TextField desctopDataSubIndex;
    public Button copyDataButton;
    public Button saveInDictionaryButton;
    public Button saveInDictionaryButton2;

    public TextField deviceDataComand;
    public TextField deviceDataValue;
    public TextField deviceDataIndex;
    public TextField deviceDataSubIndex;

    public ListView dictionaryList;
    public Button loadDictionaryButton;
    public Button saveDictionaryButton;
    public CheckBox autoSendFrameButton;
    public CheckMenuItem autoScrollMenuItem;
    public TextField timeOut;

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
    public ObservableList<String> outgoingMessages = FXCollections.observableArrayList();
    public ObservableList<String> consoleMessages = FXCollections.observableArrayList();
    private ObservableList<String> dictionaryObservList = FXCollections.observableArrayList();

    //строки
    private static String incomingString;
    private static String savedIncomingString = "";

    //счетчики
    private static int countMessages;
    private static int callsOfGetMessageMethod;

    //флаги
    private static boolean autoGetMessageFlag = false;
    private boolean isFirstAutoSend = true;

    //потоки
    AutoSendFrameThread autoSendFrameThread;

    static int itimeOut = 50;

    public void initialize() {

        //получаем список всех доступных на момент запуска проги com-портов
        final String[] portNames = SerialPortList.getPortNames();

        /*ДЛЯ ПОСЛЕДУЮЩИХ ВЕРСИЙ!:
        Для обновления списка устройств нужно использовать библиотеку https://code.google.com/archive/p/javahidapi/ .
        Далее нужно создать новый поток и контролировать в нем обновление списка подключенных устройств.*/

        //проверка на факт отсутствия доступных ком-портов
        if (portNames.length == 0) {
            System.out.println("Не найдено ни одного доступного ком-порта.");
            Messages.printInConsole(this, "Не найдено ни одного доступного ком-порта.");
            System.out.println("Нажмите Enter для выхода...");
            Messages.printInConsole(this, "Нажмите Enter для выхода...");
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

        //инициализация словаря
        dictionaryList.setItems(dictionaryObservList);
        dictionaryList.setFixedCellSize(25);

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
        final SampleController sampleController = this;
        portNumberCB.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                System.out.println("Старый порт: "+portNames[(Integer)oldValue]);
                Messages.printInConsole( sampleController,"Старый порт: "+portNames[(Integer)oldValue]);
                System.out.println("Новый порт: "+portNames[(Integer)newValue]);
                Messages.printInConsole(sampleController,"Новый порт: "+portNames[(Integer)newValue]);

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

        //слушатели листов сообщений
        consoleListView.selectionModelProperty().addListener(new ChangeListener() {
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if (autoScrollMenuItem.isSelected())
                    consoleListView.scrollTo(consoleMessages.size());
            }
        });

        sendedPacketsList.selectionModelProperty().addListener(new ChangeListener() {
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if (autoScrollMenuItem.isSelected())
                    sendedPacketsList.scrollTo(outgoingMessages.size());
            }
        });

        //слушатели текстфилдов
        desctopDataValue.textProperty().addListener(new ChangeListener<String>() {
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*(\\.\\d*)?")) {
                    desctopDataValue.setText(oldValue);
                }
            }
        });

        //поток автоотправки пакетов
        autoSendFrameThread = new AutoSendFrameThread("asend", dictionaryObservList, serialPort, this);
        autoSendFrameThread.start();
    }
/*
    public void printInConsole(String message){
        Date date = new Date();
        message = date.getTime()+": "+message;
        consoleMessages.add(message);
        consoleListView.scrollTo(consoleMessages.size());
    }
*/
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
            Messages.printInConsole(this,"Ошибка инициализации порта! "+ exc);
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
            Messages.printInConsole(this,"Не удалось применить новые настройки ком-порта! "+ exc);
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
            if (serialPort != null && serialPort.isOpened()){
                serialPort.purgePort(1);
                serialPort.purgePort(2);
                serialPort.closePort();
                //serialPort.removeEventListener();
            }
        }catch(SerialPortException exc){
            System.out.println("Ошибка завершения работы порта! "+ exc);
            Messages.printInConsole(this,"Ошибка завершения работы порта! "+ exc);
        }
    }

    private void sendMessage(String message){
        System.out.println("Попытка отправки сообщения \""+message+"\":");
        Messages.printInConsole(this,"Попытка отправки сообщения \""+message+"\":");
        message = message + (char)13;   //(char)13 = 0Dh - возврат каретки в ASCII
        try{
            Messages.printStringHexCodes(this, message);
            boolean isSucceed = serialPort.writeString(message);
            if (isSucceed == true) {
                System.out.println("Отправка успешна.");
                Messages.printInConsole(this,"Отправка успешна");
            }
            else {
                System.out.println("Отправка не удалась. Проверьте правильность подключения COM-порта.");
                Messages.printInConsole(this,"Отправка не удалась. Проверьте правильность подключения COM-порта.");
            }
        } catch (Exception exc){
            System.out.println("Ошибка передачи сообщения! "+ exc);
            Messages.printInConsole(this,"Ошибка передачи сообщения! "+ exc);
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
        if (autoScrollMenuItem.isSelected())
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
            //stopThread(); // ДЛЯ ОТЛАДКИ - ПОТОМ УДАЛИТЬ!
            readData(message);

            //обновляем обсервабллист
            incomingMessages.add(incomingString);
            if (autoScrollMenuItem.isSelected())
                recievedPacketsList.scrollTo(incomingMessages.size());

            callsOfGetMessageMethod++;
            //автоматическая прокрутка до последнего сообщения
            savedIncomingString = incomingString;
        }
    }

    private void readData(String message){
        message.replaceAll("[\\D]", "");
        byte buff[] = message.getBytes();
        String strbuf = "";

        for (int i = 5; i < 7; i++) {
            strbuf = strbuf + (char)buff[i];
        }
        //deviceDataComand.setText(""+(Integer.parseInt(strbuf, 16)));
        deviceDataComand.setText(strbuf);
        strbuf = "";

        for (int i = 7; i < 9; i++) {
            strbuf = strbuf + (char)(buff[i]);
        }
        //deviceDataIndex.setText(""+(Integer.parseInt(strbuf, 16)));
        deviceDataIndex.setText(strbuf);
        String s = strbuf;
        System.out.println(s);
        strbuf = "";

        for (int i = 9; i < 13; i++) {
            strbuf = strbuf + (char)(buff[i]);
        }
        //deviceDataSubIndex.setText(""+(Integer.parseInt(strbuf, 32)));
        deviceDataSubIndex.setText(strbuf);
        strbuf = "";

        //Данные - парсим флоат только для ряда параметров, смотрим по индексу
        for (int i = 13; i < 21; i++) {
            strbuf = strbuf + (char) (buff[i]);
        }
        if (s.equals("02") || s.equals("04") || s.equals("05") || s.equals("06") || s.equals("07") || s.equals("15") || s.equals("16"))
            deviceDataValue.setText(parseHex(strbuf) + "");
        else
            deviceDataValue.setText(strbuf);
    }

    private float parseHex(String myString){

        Long i = Long.parseLong(myString, 16);
        Float f = Float.intBitsToFloat(i.intValue());
        //System.out.println(f);
        //System.out.println(Integer.toHexString(Float.floatToIntBits(f)));

        return f;
    }
    public void exitAction(ActionEvent actionEvent) {
        try {
            closePorts();
        } catch (SerialPortException exc) {
            exc.printStackTrace();
        } finally {
            stopThread();
            Platform.exit();

        }
    }

    public void stopThread(){
        autoSendFrameThread.setFlag(false);
    }

    public void closePorts() throws SerialPortException{
        for (int i = 0; i < serialPorts.size(); i++) {
            shutdownPort(serialPorts.get(i));
        }
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
            strbuf = strbuf.replaceAll("\\s","");
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
            strbuf = strbuf.replaceAll("\\s","");
            strresult = strresult+strbuf;

            generatedTextTextField.setText(strresult);
        }
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

    public void copyMessageAction(ActionEvent actionEvent) {
        outcomingPacket.setText(generatedTextTextField.getText());
    }

    public void copyDataAction(ActionEvent actionEvent) {

        //stopThread(); // ДЛЯ ОТЛАДКИ - ПОТОМ УДАЛИТЬ!

        String data;
        String buf = "";
        //команда
        //int comand = Integer.parseInt(desctopDataComand.getText());
        //buf = Integer.toHexString(comand).toUpperCase()+"";
        buf = desctopDataComand.getText();
        if (buf.length() > 1)
            data = buf;
        else
            data = "0"+buf;

        //index
        String s;
        //int index = Integer.parseInt(desctopDataIndex.getText());
        //buf = Integer.toHexString(index).toUpperCase();
        buf = desctopDataIndex.getText();
        if (buf.length() > 1) {
            data = data + buf;
            s = buf;
        }
        else {
            data = data + "0" + buf;
            s = "0"+buf;
        }

        //subIndex
        //long subIndex = Long.parseLong(desctopDataSubIndex.getText());
        //buf = Long.toHexString(subIndex).toUpperCase();
        buf = desctopDataSubIndex.getText();
        if (buf.length() == 4)
            data = data+buf;
        else if (buf.length() == 1)
            data = data+"000"+buf;
        else if (buf.length() == 2)
            data = data+"00"+buf;
        else if (buf.length() == 3)
            data = data+"0"+buf;

        //значение параметра
        float value;
        if (s.equals("02") || s.equals("04") || s.equals("05") || s.equals("06") || s.equals("07") || s.equals("15") || s.equals("16")) {
            value = Float.parseFloat(desctopDataValue.getText());
            data = data + Integer.toHexString(Float.floatToIntBits(value)).toUpperCase(); //трансформация float значения в строку байт IEEE-754
        } else{
            data = data + desctopDataValue.getText();
        }
        desktopData.setText(data);
    }

    public void regexChars(ActionEvent actionEvent){
        TextField textField = (TextField)actionEvent.getSource();
        String text = textField.getText();
        System.out.println(actionEvent.getSource().toString());
        text = text.replaceAll("[a-zA-Z]", "");
        textField.setText(text);

    }

    public void saveInDictionaryAction(ActionEvent actionEvent) {
        dictionaryObservList.add(outcomingPacket.getText());
        autoSendFrameThread.setList(dictionaryObservList);
    }

    public void saveInDictionaryAction2(ActionEvent actionEvent) {
        dictionaryObservList.add(incomingPacket.getText());
        autoSendFrameThread.setList(dictionaryObservList);

    }

    public void loadDictionaryAction(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        File file = fileChooser.showOpenDialog(null);
        if(file != null){
            //textAreaOne.setText(readFile(file));
            readFile(dictionaryObservList, file);
        }
    }

    private String readFile(ObservableList<String> list, File file){

        StringBuilder stringBuffer = new StringBuilder();
        BufferedReader bufferedReader = null;

        try {
            bufferedReader = new BufferedReader(new FileReader(file));

            String text;
            dictionaryObservList.clear();
            while ((text = bufferedReader.readLine()) != null) {
                list.add(text);
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(SampleController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SampleController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException ex) {
                Logger.getLogger(SampleController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return stringBuffer.toString();
    }

    public void saveDictionaryAction(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);
        //Show save file dialog
        File file = fileChooser.showSaveDialog(null);
        if(file != null){
            SaveFile(dictionaryObservList, file);
        }
    }

    private void SaveFile(ObservableList<String> list, File file){
        try{
            FileWriter fileWriter = new FileWriter(file);

            for (int i = 0; i < list.size(); i++) {
                fileWriter.write(list.get(i)+"\r\n");
            }
            fileWriter.close();
        }catch (Exception exc){
            Logger.getLogger(SampleController.class.getName()).log(Level.SEVERE, null, exc);
        }
    }

    public void autoSendFrameAction(ActionEvent actionEvent) {
        System.out.println("autosending is " + autoSendFrameButton.isSelected());

        if (autoSendFrameButton.isSelected() == true && !(dictionaryObservList.get(0) == null)) {
            autoSendFrameThread.setList(dictionaryObservList);
            autoSendFrameThread.setSendOn(true);
        }else
            autoSendFrameThread.setSendOn(false);
    }

    public void makeOutcomingPacket(ActionEvent actionEvent) {
        outcomingPacket.setText(dictionaryObservList.get(dictionaryList.getSelectionModel().getSelectedIndex()));
    }

    public void deleteSelected(ActionEvent actionEvent) {
        dictionaryObservList.remove(dictionaryObservList.get(dictionaryList.getSelectionModel().getSelectedIndex()));
        autoSendFrameThread.setList(dictionaryObservList);
    }

    public void autoscrollAction(ActionEvent actionEvent) {
        if (autoScrollMenuItem.isSelected()) {
            consoleListView.scrollTo(consoleMessages.size());
            sendedPacketsList.scrollTo(outgoingMessages.size());
            recievedPacketsList.scrollTo(incomingMessages.size());

        } else if (!autoScrollMenuItem.isSelected()){
            consoleListView.scrollTo(consoleMessages.size()-10);
            sendedPacketsList.scrollTo(outgoingMessages.size()-10);
            recievedPacketsList.scrollTo(incomingMessages.size()-10);
        }
    }

    public void timeOutAction(KeyEvent keyEvent) {
        String text = timeOut.getText();
        itimeOut = Integer.parseInt(text.replaceAll("[\\D]", ""));
        if (itimeOut < 50) {
            itimeOut = 50;
            //timeOut.setText(""+itimeOut);
        } else if (itimeOut >1000){
            itimeOut = 1000;
            //timeOut.setText(""+itimeOut);
        }
        autoSendFrameThread.setTimeOut(itimeOut);
    }

    public void readDataCMAction(ActionEvent actionEvent) {
        String s;
        s = incomingMessages.get(recievedPacketsList.getSelectionModel().getSelectedIndex());
        readData(s);
    }

    public void readDataCMAction1(ActionEvent actionEvent) {
        String s;
        s = outgoingMessages.get(sendedPacketsList.getSelectionModel().getSelectedIndex());
        readData(s);
    }

    public void saveInDictionaryAction3(ActionEvent actionEvent) {
        String s;
        s = outgoingMessages.get(sendedPacketsList.getSelectionModel().getSelectedIndex());
        dictionaryObservList.add(s);
        autoSendFrameThread.setList(dictionaryObservList);
    }

    public void saveInDictionaryAction4(ActionEvent actionEvent) {
        String s;
        s = incomingMessages.get(recievedPacketsList.getSelectionModel().getSelectedIndex());
        dictionaryObservList.add(s);
        autoSendFrameThread.setList(dictionaryObservList);
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
                        Messages.printInConsole(sampleController, finMessage);
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


/*
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
*/