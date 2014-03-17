package com.example.btcar;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;

public class btcon {
	private static final UUID HeadSet_UUID = UUID.fromString("0000111E-0000-1000-8000-00805F9B34FB"); 
	private static final UUID OBEX_UUID = UUID.fromString("00001105-0000-1000-8000-00805F9B34FB");
	private static final UUID HID_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private static final UUID    Seial_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");	
	private final String SD_PATH = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
	
	private static final String prdName="JACK";
	BluetoothAdapter btAdapt;
	public static BluetoothSocket btSocket;
	BluetoothDevice btDev;
	int btstatus;
	Activity act;
	   btcon(Activity view)
	    {
	        btAdapt = BluetoothAdapter.getDefaultAdapter();
	        btAdapt.enable();
	        act=view;
	        if(!Search(prdName))
	        	Discovery();
	    }
	   public int  btStatus()
	   {
		   
		   return btstatus;
	   }
	    public boolean Search(String prd)
	    {   	
	    	String cmd="0x1000 0x2 0xaa55 0x433";
	    	 Set<BluetoothDevice> pairedDevices = btAdapt.getBondedDevices();
	         if (pairedDevices.size() > 0) {
	             for (BluetoothDevice device : pairedDevices) {
	                 String name= device.getName();
	              	if(name.startsWith(prd))
	              	{
	              		btDev=device;
	              		if(SendCmd(cmd))
	              			return true;
	              	}
	                }
	           }  
	         return false;
	    }
	    
	    public boolean SendCmd(String cmd)
	    {
	    	 Context  mContext = act;
	    	   try {

	               Method m = null;
	               try {
	                   m = btDev.getClass().getMethod("createRfcommSocket", new Class[] { int.class });
	               } catch (SecurityException e) {
	                   e.printStackTrace();
	                   return false;
	               } catch (NoSuchMethodException e) {
	                   e.printStackTrace();
	                   return false;
	               }
	               try {
	                   btSocket = (BluetoothSocket) m.invoke(btDev, 1);
	               } catch (IllegalArgumentException e) {
	                   e.printStackTrace();
	                   return false;
	               } catch (IllegalAccessException e) {
	                   e.printStackTrace();
	                   return false;
	               } catch (InvocationTargetException e) {
	                   e.printStackTrace();
	                   return false;
	               }
	               Uri stream;
	               String targetAddr=btDev.getAddress();
	               String FilePath=SD_PATH+"/1.txt";
	               FileWriter fw=new FileWriter(FilePath);
	               File fp=new File(FilePath);
	          //     boolean f =fp.exists();
	         //      fp.delete();
	           //    fp.createNewFile();
	              
	               fw.write(cmd);
	               fw.flush();
	               fw.close();
	              
	               
	               stream=Uri.fromFile(fp);

	               ContentValues cv = new ContentValues();
	               cv.put("uri", stream.toString());
	               cv.put("destination", targetAddr);
	               cv.put("direction", 0);
	               Long ts = System.currentTimeMillis();
	               cv.put("timestamp", ts);
	             //  cv.put("mimetype","audio/wav");
	               mContext.getContentResolver().insert(Uri.parse("content://com.android.bluetooth.opp/btopp"),cv);
	               
	               btSocket.close();
	           } catch (IOException e) {
	               e.printStackTrace();
	               return false;
	           }
	    	   return true;
	    }
	    
	    private void RegEvent()
	    {
	    	IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
	    	act.registerReceiver(mReceiver, filter);
	    	filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
  			act.registerReceiver(mReceiver, filter);
  			filter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
  			act.registerReceiver(mReceiver, filter);
  			filter = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
  			act.registerReceiver(mReceiver, filter);   
	    }
	   public boolean Discovery()
	    {
	    	if (btAdapt.isDiscovering()) {
	    		btAdapt.cancelDiscovery();
	        }
	    	IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
	    	act.registerReceiver(mReceiver, filter);
	    	filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
	        act.registerReceiver(mReceiver, filter);  
	        btAdapt.startDiscovery();
	        return true;
	    }
	    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	        @Override
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        
	        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
	        
	            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	            String name= device.getName();
	            if	(name.startsWith("8XX")||name.startsWith(prdName))
	            {
	            	if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
	            		try{
	            			Method createBondMethod = BluetoothDevice.class.getMethod("createBond");   
	                        Boolean ret =(Boolean) createBondMethod.invoke(device);    
	     //                   Toast.makeText(getApplicationContext(), name + "added to list", Toast.LENGTH_LONG).show();
	            		    
	            		}catch (Exception e) {   
	                        e.printStackTrace();   
	                    }   
	            	}                	
	            }
	        // 當發現藍牙裝置結束時，更改機動程式的標題
	        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
	        
	        } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
	        //	Log.d("BlueToothTestActivity", "ACTION_FOUND");   
	        } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
	        	BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	            String name= device.getName();
	      	    if(name.startsWith("8XX")||name.startsWith(prdName))
	      	    {
	//                Toast.makeText(getApplicationContext(), name+"Device connected", Toast.LENGTH_LONG).show();
	      	    }    
	        } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
	        	BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	            String name= device.getName();
	            if(name.startsWith("8XX")||name.startsWith(prdName))
	      	    {

	      	    }   
	        }
	        
	    }}; 	

	   
}
