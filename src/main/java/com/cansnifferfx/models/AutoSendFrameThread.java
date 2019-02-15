package com.cansnifferfx.models;

import com.cansnifferfx.controllers.SampleController;
import javafx.collections.ObservableList;
import jssc.SerialPort;

public class AutoSendFrameThread extends Thread {

    public AutoSendFrameThread(String name, ObservableList<String> list, SerialPort serialPort, SampleController controller){
        super(name);
        this.list = list;
        listSize = list.size();
        this.serialPort = serialPort;
        this.controller = controller;
        System.out.println(name+" has been made!");
    }

    private int index = 0;
    private int listSize;
    ObservableList<String> list;
    SampleController controller;
    SerialPort serialPort;
    private boolean sendOn = false;
    private boolean flag = true;
    private long cicle = 0;
    private int timeout = 50;

    public boolean isSendOn() {
        return sendOn;
    }

    public void setSendOn(boolean sendOn) {
        this.sendOn = sendOn;
    }

    public ObservableList<String> getList() {
        return list;
    }

    public void setList(ObservableList<String> list) {
        this.list = list;
        index = 0;
        listSize = list.size();
    }

    public void setTimeOut(int timeout){
        this.timeout = timeout;
    }

    @Override
    public void run() {
        while (flag) {
            System.out.println("Метод run() потока "+this.getName()+", цикл "+cicle+", индекс "+index+", listsize "+listSize);
            if (sendOn) {
                //послать в порт
                System.out.println("sending frame №" + index + "...");
                if (listSize > 0) {
                    try {
                        Messages.sendMessage(controller, list.get(index), serialPort);
                        Thread.sleep(timeout);        //Приостановка потока на 1 сек.
                    } catch (InterruptedException e) {
                        System.out.println(e.toString());
                    }
                    index++;
                    if (index == listSize)
                        index = 0;
                }
            }
            cicle++;
        }
    }
}
