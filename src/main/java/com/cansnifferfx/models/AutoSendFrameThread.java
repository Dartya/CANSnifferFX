package com.cansnifferfx.models;

import javafx.collections.ObservableList;

public class AutoSendFrameThread extends Thread {

    public AutoSendFrameThread(String name, ObservableList<String> list){
        super(name);
        this.list = list;
        listSize = list.size();
    }

    private int index = 0;
    private int listSize;
    ObservableList<String> list;

    public ObservableList<String> getList() {
        return list;
    }

    public void setList(ObservableList<String> list) {
        this.list = list;
        index = 0;
        listSize = list.size();
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void run(){
        while (index < listSize){
            //послать в порт list.get(index); // для этого нужно инкапсулировать метод отправки пакета
            index++;
            if (index == listSize)
                index = 0;
        }
    }
}
