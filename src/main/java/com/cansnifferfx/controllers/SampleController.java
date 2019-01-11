package com.cansnifferfx.controllers;

import javafx.event.ActionEvent;
import javafx.scene.control.*;

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
