/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wly.net;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView.OnEditorActionListener;
import android.widget.*;
public class PortScannerActivity extends FragmentActivity 
        implements OnEditorActionListener, PortListFragment.OnPortSelectedListener {
	
	// JNI library methods
	public native boolean sendPacket();
	public native short computeCheckSum(int nwords);
	public native boolean buildIpHeader(IpHeader ipHeader);
	public native boolean buildTcpHeader(TcpHeader tcpHeader);

	static final String TAG = "PortScannerActivity";
	static String host = null;
	static String ipAddress = null;
	static List<Integer> portList = new ArrayList<Integer>();
	public static List<Integer> getPortList() { return portList; }
	static IpHeader ip; // = new IpHeader((char)5, (char)4, (char)0,(char)0, (char)0, (short)0,(short)0,(short)0, 0, 0, 0);
	static TcpHeader tcp; // = new TcpHeader((short)1, (short)2, (short)3,(short)4, (short)5, (short)6,(short)7,(short)8,
			// (short)9, (short)10,(short)11, (short)12, (short)13,(short)14,(short)15, (long)1234, (long)4321);
	private static Bundle datagramBundle = new Bundle();
	
	public static void createPacketDatagram(Bundle pktDatagram)	{
		ip = new IpHeader();
		ip.setIhl(pktDatagram.getChar("ihl"));
		ip.setVer(pktDatagram.getChar("version"));
		ip.setTos(pktDatagram.getChar("tos"));
		ip.setVer(pktDatagram.getChar("ttl"));
		ip.setTos(pktDatagram.getChar("protocol"));
		ip.setTotalLen(pktDatagram.getShort("tot_len"));
		ip.setId(pktDatagram.getShort("id"));
		ip.setTotalLen(pktDatagram.getShort("frag_off"));
		ip.setIpCheck(pktDatagram.getInt("ipCheck"));
		ip.setSourceAddress(pktDatagram.getInt("saddr"));
		ip.setDestAddress(pktDatagram.getInt("daddr"));
		tcp = new TcpHeader();
		tcp.setSource(pktDatagram.getShort("source"));
		tcp.setDest(pktDatagram.getShort("dest"));
		tcp.setDoff(pktDatagram.getShort("doff"));
		tcp.setRes1(pktDatagram.getShort("res1"));
		tcp.setCwr(pktDatagram.getShort("cwr"));
		tcp.setEce(pktDatagram.getShort("ece"));
		tcp.setUrg(pktDatagram.getShort("urg"));
		tcp.setAck(pktDatagram.getShort("ack"));
		tcp.setPsh(pktDatagram.getShort("psh"));
		tcp.setRst(pktDatagram.getShort("rst"));
		tcp.setSyn(pktDatagram.getShort("syn"));
		tcp.setFin(pktDatagram.getShort("fin"));
		tcp.setWindow(pktDatagram.getShort("window"));
		tcp.setCheck(pktDatagram.getShort("check"));
		tcp.setUrgPtr(pktDatagram.getShort("urg_ptr"));
		tcp.setSeq(pktDatagram.getLong("seq"));
		tcp.setAckSeq(pktDatagram.getLong("ack_seq"));
		
		datagramBundle.putAll(pktDatagram);
	}
	
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.host_ports);
        
        if(savedInstanceState == null)
        	Toast.makeText(this, "No saved bundle or bundle is null.", Toast.LENGTH_LONG).show();
        else
        	createPacketDatagram(savedInstanceState);

        
		((EditText)findViewById(R.id.host_name_text)).setOnEditorActionListener(this);
		((EditText)findViewById(R.id.port_range_text)).setOnEditorActionListener(this);

        // Check whether the activity is using the layout version with
        // the fragment_container FrameLayout. If so, we must add the first fragment
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create an instance of ExampleFragment
            PortListFragment firstFragment = new PortListFragment();

            // In case this activity was started with special instructions from an Intent,
            // pass the Intent's extras to the fragment as arguments
            firstFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, firstFragment).commit();
        }
    }
    
	@Override
	public void onSaveInstanceState(Bundle savedPacketInstanceState) {
		if(!datagramBundle.isEmpty())
			savedPacketInstanceState.putAll(datagramBundle);
		else
			Toast.makeText(this, "The datagram bundle is empty!", Toast.LENGTH_LONG).show();
	}
	
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.action_bar_menu, menu);
		return true;
	}
    
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = true;
	      switch (item.getItemId()) {
	      case R.id.packet_action:
	    	  Intent ipInfoIntent = new Intent(PortScannerActivity.this, PacketInfoActivity.class);
	    	  //myIntent.putExtra("key", value); //Optional parameters
	    	  PortScannerActivity.this.startActivity(ipInfoIntent);
	    	  break;
		     case R.id.scan_action:
		        Toast.makeText(this, "Scan Clicked", Toast.LENGTH_SHORT).show();
		        try	{
					//@SuppressWarnings("unused")
		        	if(ip == null)
		        		Log.d(TAG, "ip is null");
		        	else
		        		result = buildTcpHeader(tcp);
		        	}
				catch(Exception e) {
					e.printStackTrace();
				}
		        break;
	
		      case R.id.stop_action:
		
		    	  Toast.makeText(this, "Stop Clicked", Toast.LENGTH_SHORT).show();
		    	    
		    	  break;
		     	  
		       default:
		    	   break;
	      }
      return result;
    }
	public void onPortSelected(int position) {
        // The user selected the headline of an article from the HeadlinesFragment

        // Capture the article fragment from the activity layout
        PortInfoFragment portInfoFrag = (PortInfoFragment)
                getSupportFragmentManager().findFragmentById(R.id.port_info_fragment);

        if (portInfoFrag != null) {
            // If article frag is available, we're in two-pane layout...

            // Call a method in the ArticleFragment to update its content
        	portInfoFrag.updatePortInfo(position);

        } else {
            // If the frag is not available, we're in the one-pane layout and must swap frags...

            // Create fragment and give it an argument for the selected article
            PortInfoFragment newFragment = new PortInfoFragment();
            Bundle args = new Bundle();
            args.putInt(PortInfoFragment.ARG_POSITION, position);
            newFragment.setArguments(args);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack so the user can navigate back
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);

            // Commit the transaction
            transaction.commit();
        }
    }

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		boolean result = true;
		if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE) {
			// the user is done typing.
		 
			switch (v.getId()) {
			case R.id.host_name_text:
				Toast.makeText(this, "In host_name_text case statement.", Toast.LENGTH_SHORT).show();
				host = ((EditText)v).getText().toString();
				if(validateUrl(host))
					Toast.makeText(this, "The url " + host + " is valid.", Toast.LENGTH_SHORT).show();	
				if(validateIp(host))
					Toast.makeText(this, "The ip address " + host + " is valid.", Toast.LENGTH_SHORT).show();
			break;
			case R.id.port_range_text:
		  	parsePorts(((EditText)v).getText().toString());
		  	BaseAdapter adapter = (BaseAdapter) ((PortListFragment) getSupportFragmentManager().findFragmentById(R.id.port_list_fragment)).getListAdapter();
		    
			adapter.notifyDataSetInvalidated();
			adapter.notifyDataSetChanged();
			
			
			break;
			default:
				result = false;
				break;
			
			}
		  
		}
			return result; // pass on to other listeners. 
		 
	}
	
	private void parsePorts(String string) {
		portList.clear();
		String[] ports = string.split(",");
    //Toast.makeText(this,"Ports string: "+ ports[0], Toast.LENGTH_LONG).show();
		for(String s : ports) {
			 if(s.contains("-")) { 
					String[] temp = s.split("-");
					for(int i = Integer.parseInt(temp[0]);i <= Integer.parseInt(temp[1]); i++)
						portList.add( Integer.valueOf(i));
			}
			else
				portList.add(Integer.valueOf((Integer.parseInt(s))));
		}
		// To Do: sort ports descending
		Collections.sort(portList);
		//for(Integer i : portList)
			//Toast.makeText(this,"Port# " + i.intValue(),Toast.LENGTH_SHORT).show();
				
	}

	// Check if ip address is valid.
	private boolean validateIp(final String ip) {
		boolean result = Patterns.IP_ADDRESS.matcher(ip).matches();
		Log.i(TAG, " Validation of " + ip + " returned " + String.valueOf(result));
		return result;
	}
	

		// Check if the URL is valid.
		private boolean validateUrl(final String url) {
				boolean result = Patterns.DOMAIN_NAME.matcher(url).matches();
				Log.i(TAG, " Validation of " + url + " returned " + String.valueOf(result));
				return result;
		}


    /* this is used to load the 'hello-jni' library on application
     * startup. The library has already been unpacked into
     * /data/data/com.example.hellojni/lib/libhello-jni.so at
     * installation time by the package manager.
     */
    static {
    	try	{
				//Thread. sleep(45 * 1000);
				System.loadLibrary("packetbuilder");
    	}
    	catch(UnsatisfiedLinkError e) {
    		e.printStackTrace();
    	}
			//catch(InterruptedException e)	{
			//	e.printStackTrace();	}
			//}
    
	}
}
    
   
