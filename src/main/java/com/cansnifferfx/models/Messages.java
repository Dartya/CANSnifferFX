package com.cansnifferfx.models;

import com.cansnifferfx.controllers.SampleController;
import jssc.SerialPort;

import java.util.Date;

public class Messages {
    public static void sendMessage(SampleController controller, String message, SerialPort serialPort){
        System.out.println("Попытка отправки сообщения \""+message+"\":");
        printInConsole(controller,"Попытка отправки сообщения \""+message+"\":");
        message = message + (char)13;   //(char)13 = 0Dh - возврат каретки в ASCII
        try{
            printStringHexCodes(controller, message);
            boolean isSucceed = serialPort.writeString(message);
            if (isSucceed == true) {
                System.out.println("Отправка успешна.");
                printInConsole(controller,"Отправка успешна");
            }
            else {
                System.out.println("Отправка не удалась. Проверьте правильность подключения COM-порта.");
                printInConsole(controller,"Отправка не удалась. Проверьте правильность подключения COM-порта.");
            }
        } catch (Exception exc){
            System.out.println("Ошибка передачи сообщения! "+ exc);
            printInConsole(controller,"Ошибка передачи сообщения! "+ exc);
        }
        //работаем с вьюхами окна:
        String strbuf = "";
        byte[] buffer = message.getBytes();
        if ((controller.desktopId.getText().equals("") || controller.desktopDataSize.getText().equals("") || controller.desktopData.getText().equals("")) && !controller.outcomingPacket.getText().equals("") ){
            //устанавливаем байты идентификатора
            for (int i = 1; i < 4; i++) {
                strbuf = strbuf + (buffer[i]) + " ";
            }
            controller.desktopId.setText(strbuf);
            strbuf = "";

            //устанавливаем длину пакета
            controller.desktopDataSize.setText(String.valueOf((char) buffer[4]));

            //устанавливаем байты данных
            for (int i = 5; i < buffer.length - 1; i++) {
                strbuf = strbuf + (buffer[i]) + " ";
            }
            controller.desktopData.setText(strbuf);
        }

        //автоматическая прокрутка до последнего сообщения
        controller.outgoingMessages.add(message);
        controller.sendedPacketsList.scrollTo(controller.outgoingMessages.size());

        System.out.println("Send success!");
    }

    public static void printInConsole(SampleController controller, String message){
        Date date = new Date();
        message = date.getTime()+": "+message;
        controller.consoleMessages.add(message);
        controller.consoleListView.scrollTo(controller.consoleMessages.size());
    }

    public static void printStringHexCodes(SampleController controller, String message){
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
        printInConsole(controller, mes);
    }
}
