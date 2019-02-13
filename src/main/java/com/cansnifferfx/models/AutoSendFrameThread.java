package com.cansnifferfx.models;

import com.cansnifferfx.controllers.SampleController;
import javafx.collections.ObservableList;
import jssc.SerialPort;

public class AutoSendFrameThread extends Thread {

    public AutoSendFrameThread(String name, ObservableList<String> list){
        super(name);
        this.list = list;
        listSize = list.size();
        System.out.println(name+" has been made!");
    }

    private int index = 0;
    private int listSize;
    ObservableList<String> list;
    SampleController controller;
    SerialPort serialPort;

    public ObservableList<String> getList() {
        return list;
    }

    public void setList(ObservableList<String> list) {
        this.list = list;
        index = 0;
        listSize = list.size();
    }

    public void run(){
        while (index < listSize){
            //послать в порт
            System.out.println("sending frame №"+index+"...");
            Messages.sendMessage(controller, list.get(index), serialPort);
            index++;
            if (index == listSize)
                index = 0;
        }
    }
}
