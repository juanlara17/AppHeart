package com.juanfelo.appheart;

import android.os.Bundle;
import android.os.Message;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import zephyr.android.HxMBT.BTClient;
import zephyr.android.HxMBT.ConnectListenerImpl;
import zephyr.android.HxMBT.ConnectedEvent;
import zephyr.android.HxMBT.ZephyrPacketArgs;
import zephyr.android.HxMBT.ZephyrPacketEvent;
import zephyr.android.HxMBT.ZephyrPacketListener;
import zephyr.android.HxMBT.ZephyrProtocol;


/**
 * Created by Juanfelo on 07/04/2015.
 */

/**********SE GENERA UNA NUEVA CLASE PARA ESCUCHAR U OBTENER LAS TRAMAS PROVENIENTES DEL DISPOSITIVO*********/
public class NewConnectedListener extends ConnectListenerImpl
{
    /********DECLARACION DE VARIABLES********/
    private Handler _OldHandler;
    private Handler _aNewHandler;
    private int GP_MSG_ID = 0x20;
    private int GP_HANDLER_ID = 0x20;
    private int HR_SPD_DIST_PACKET =0x26;

    private final int HEART_RATE = 0x100;
    private final int INSTANT_SPEED = 0x101;
    private ConnectListenerImpl.HRSpeedDistPacketInfo HRSpeedDistPacket = new HRSpeedDistPacketInfo();
    public NewConnectedListener(Handler handler,Handler _NewHandler) {
        super(handler, null);
        _OldHandler= handler;
        _aNewHandler = _NewHandler;

        // TODO Auto-generated constructor stub

    }

    /******VERIFICAR ESTADO CORRECTO DE CONEXION DEL HxM CON ANDROID*******/
    public void Connected(ConnectedEvent<BTClient> eventArgs) {
        System.out.println(String.format("Connected to BioHarness %s.", eventArgs.getSource().getDevice().getName()));

        /******CREA UN NUEVO OBJETO PROTOCOLO ZEPHYR Y LO PASA AL OBJETO BTCOMMS*****/
        ZephyrProtocol _protocol = new ZephyrProtocol(eventArgs.getSource().getComms());
        
        _protocol.addZephyrPacketEventListener(new ZephyrPacketListener() {
            public void ReceivedPacket(ZephyrPacketEvent eventArgs) {
                ZephyrPacketArgs msg = eventArgs.getPacket();
                byte CRCFailStatus;
                byte RcvdBytes;


                CRCFailStatus = msg.getCRCStatus();
                RcvdBytes = msg.getNumRvcdBytes() ;
                if (HR_SPD_DIST_PACKET==msg.getMsgID())
                {


                    byte [] DataArray = msg.getBytes();

                    //***************VISUALIZANDO HEART RATE********************************
                    int HRate =  HRSpeedDistPacket.GetHeartRate(DataArray);
                    Message text1 = _aNewHandler.obtainMessage(HEART_RATE);
                    Bundle b1 = new Bundle();
                    b1.putString("HeartRate", String.valueOf(HRate));
                    text1.setData(b1);
                    _aNewHandler.sendMessage(text1);
                    System.out.println("Heart Rate is "+ HRate);

                    //***************VISUALIZANDO VELOCIDAD INSTANTANEA********************************
                    double InstantSpeed = HRSpeedDistPacket.GetInstantSpeed(DataArray);

                    text1 = _aNewHandler.obtainMessage(INSTANT_SPEED);
                    b1.putString("InstantSpeed", String.valueOf(InstantSpeed));
                    text1.setData(b1);
                    _aNewHandler.sendMessage(text1);
                    System.out.println("Instant Speed is "+ InstantSpeed);

                }
            }
        });
    }

}

