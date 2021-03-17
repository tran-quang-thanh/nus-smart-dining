package com.example.myapplication;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

public class FirstFragment extends Fragment {

    public static EditText address = null;

    final byte[] ALIGN_CENTER = {0x1b, 0x61, 0x01};
    final byte[] TEXT_SIZE_NORMAL = {0x1b, 0x21, 0x00};
    final byte[] TEXT_SIZE_LARGE = {0x1b, 0x21, 0x30};
    final byte[] INIT = {0x1b, 0x40};
    final byte[] BEEPER = {0x1b,0x42,0x05,0x09};
    final byte[] TXT_BOLD_ON= {0x1b,0x45,0x01};
    final byte[] LEFT_MARGIN = {0x1b, 0x6c, 3};
    final byte[] LINE_SPACE_1_360_INCH = {0x1b, 0x2b, 1};
    final byte[] LINE_SPACE_1_6_INCH = {0x1b, 0x32};

    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    Socket socket;

    OutputStream outputStream;
    InputStream inputStream;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        View fragmentFirstLayout = inflater.inflate(R.layout.fragment_first, container, false);
        address = (EditText)fragmentFirstLayout.findViewById(R.id.address);
        return fragmentFirstLayout;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        view.findViewById(R.id.button_first).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                NavHostFragment.findNavController(FirstFragment.this)
//                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
//            }
//        });

        view.findViewById(R.id.print_bluetooth).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new PrintBluetoothTask().execute();
            }
        });


        view.findViewById(R.id.print_wifi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new PrintWifiTask().execute();
            }
        });

    }

    private class PrintWifiTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                String header = "Please present this receipt at the collection " +
                        "counter when your number is displayed\n";
                String order = "Your order number is\n";
                String orderNumber = "A139\n\n";
                String foodCounter = "Western";
                StringBuilder detail = new StringBuilder();

                Socket socket = new Socket(address.getText().toString(), 9100);              //one socket responsible for one device
                outputStream = socket.getOutputStream();

                outputStream.write(ALIGN_CENTER);
                outputStream.write(TXT_BOLD_ON);
                outputStream.write(header.getBytes("GBK"));                      //when printing text, "write()" will print before "println()"
                outputStream.write(new byte[]{0x0a});

                outputStream.write(order.getBytes("GBK"));

                outputStream.write(TEXT_SIZE_LARGE);
                outputStream.write(orderNumber.getBytes("GBK"));

                outputStream.write(TEXT_SIZE_NORMAL);
                outputStream.write(foodCounter.getBytes("GBK"));

                outputStream.write(new byte[]{0x0a});
                outputStream.write(new byte[]{0x0a});

                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String currentDateTimeString = df.format(new Date());
                detail.append("Bill No :                               389810\n");
                detail.append("Date:   :                  " + currentDateTimeString + "\n");
                outputStream.write(detail.toString().getBytes("GBK"));
                detail = new StringBuilder();
                detail.append("Order Type :                           Dine_In\n");
                detail.append("----------------------------------------------\n");
                outputStream.write(detail.toString().getBytes("GBK"));
                detail = new StringBuilder();

                detail.append("Sales Items\n");
                detail.append("1. Black Pepper Grilled Chicken Chop   3.60 *1\n");
                outputStream.write(detail.toString().getBytes("GBK"));
                detail = new StringBuilder();
                detail.append("25.Add Sunshine Egg                    0.60 *1\n");
                detail.append("----------------------------------------------\n");
                outputStream.write(detail.toString().getBytes("GBK"));
                detail = new StringBuilder();

                detail.append("Sub-Total Amount:                     SGD 4.20\n");
                detail.append("Total Qty       :                            2\n");
                outputStream.write(detail.toString().getBytes("GBK"));

                detail = new StringBuilder();
                detail.append("Total Payable   :                     SGD 4.20\n");
                detail.append("Paid By         :  NETS (DBS OCBC UOB NETSPay)\n");

                outputStream.write(detail.toString().getBytes("GBK"));
                detail = new StringBuilder();

                outputStream.write(new byte[]{0x0a});

                detail.append("Thank You\n");
                detail.append("Please come again\n");

                outputStream.write(detail.toString().getBytes("GBK"));

                outputStream.write(new byte[]{0x1d, 0x56, 0x41, 0x10});                    //"0x1d, 0x56, 0x41" is for paper cut and "0x10" is for line feed

                outputStream.write(BEEPER);                                               //hardware turn on

                outputStream.write(INIT);

                outputStream.close();

                socket.close();
            } catch (UnknownHostException e) {
                Log.e("Print()", "UnknownHostException");
            } catch (IOException e) {
                if (getActivity() != null) {
                    Snackbar.make(getActivity().findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_SHORT)
                            .show();
                }
            }
            return null;
        }
    }

    private class PrintBluetoothTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                String milkTeaBrand = "Gong Cha ";
                String orderNumber = "00007686\n";
                String orderInfoChinese = "Alt Text";
                String orderInfo1 = "GCM Alisan Tea";
                String orderInfo2 = "5 X Pearl Jelly";
                String orderInfo3 = "3S";
                StringBuilder detail = new StringBuilder();

                findBluetooth();
                openBluetooth();

                outputStream.write(LEFT_MARGIN);
                outputStream.write(milkTeaBrand.getBytes("GBK"));                      //when printing text, "write()" will print before "println()"
                outputStream.write(orderNumber.getBytes("GBK"));
                outputStream.write(LINE_SPACE_1_360_INCH);

                @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
                String currentDateTimeString = df.format(new Date());
                detail.append(currentDateTimeString + "\n");
                outputStream.write(detail.toString().getBytes("GBK"));
                detail = new StringBuilder();
                detail.append("______________________________\n");
                outputStream.write(detail.toString().getBytes("GBK"));
                outputStream.write(new byte[]{0x0a});

                outputStream.write(LINE_SPACE_1_6_INCH);
                detail = new StringBuilder();
                detail.append(orderInfoChinese + "\n");
                detail.append(orderInfo1 + "\n");
                detail.append(orderInfo2 + "\n");
                detail.append(orderInfo3 + "\n");

                outputStream.write(detail.toString().getBytes("GBK"));

                outputStream.write(new byte[]{0x1d, 0x56, 0x41, 0x10});                    //"0x1d, 0x56, 0x41" is for paper cut and "0x10" is for line feed

                outputStream.write(BEEPER);                                               //hardware turn on

                outputStream.write(INIT);

                outputStream.close();

                closeBluetooth();
            } catch (UnknownHostException e) {
                Log.e("Print()", "UnknownHostException");
            } catch (IOException e) {
                if (getActivity() != null) {
                    Snackbar.make(getActivity().findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_SHORT)
                            .show();
                }
            }
            return null;
        }

        // tries to open a connection to the bluetooth printer device
        void openBluetooth() throws IOException {
            try {

                // Standard SerialPortService ID
                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
                mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
                mmSocket.connect();
                outputStream = mmSocket.getOutputStream();
                inputStream = mmSocket.getInputStream();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // close the connection to bluetooth printer.
        void closeBluetooth() throws IOException {
            try {
                outputStream.close();
                inputStream.close();
                mmSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // this will find a bluetooth printer device
        void findBluetooth() {

            try {
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                if (mBluetoothAdapter == null) {
                    System.out.println("No bluetooth adapter available");
                }

                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBluetooth, 0);
                }

                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

                if (pairedDevices.size() > 0) {
                    for (BluetoothDevice device : pairedDevices) {
                        if (device.getAddress().equals(address.getText().toString().toUpperCase()) ||
                                device.getName().equals(address.getText().toString())) {
                            mmDevice = device;
                            break;
                        }
                    }
                }
                System.out.println("Bluetooth device found.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}