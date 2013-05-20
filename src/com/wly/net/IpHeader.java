package com.wly.net;

// The IP header's structure 

public class IpHeader
{
	public IpHeader(byte ihl, byte version, byte tos, byte ttl,short check, short tot_len,
			short id, float saddr, float daddr) {
					this.ihl = ihl;
					this.version = version;
					this.tos = tos;
					this.ttl = ttl;
          this.check = check;
					this.tot_len = tot_len;
					this.saddr = saddr;
					this.daddr = daddr;
	}

	public native boolean buildIpHeader(IpHeader ipHeader);
	
	private byte ihl, version, tos, ttl;
	public  byte getIhl(){ return ihl; }
	public  void setIhl(byte ihl){ this.ihl = ihl; }

	public  byte getVer(){ return version; }
	public  void setVer(byte version){ this.version = version; }

	public  byte getTos(){ return tos; }
	public  void setTos(byte tos){ this.tos = tos; }

	public  byte getTtl(){return ttl; }
	public  void setTtl(byte ttl){this.ttl = ttl; }

	private short check, tot_len, id;
	public  short getChksum(){ return check; }
	public  void setChksum(short check){ this.check = check; }

  	public  short getTotalLength(){ return tot_len; }
	public  void setTotalLength(short tot_len){ this.tot_len = tot_len; }
  
  	public  short getId(){ return id; }
	public  void setId(short id){ this.id = id; }
  
	private float saddr, daddr;
	public  float getSourceAddress(){ return saddr; }
	public  void setSourceAddress(float saddr){ this.saddr = saddr; }

	public  float getDestAddress(){ return daddr; }
	public  void setDestAddress(float daddr){ this.daddr = daddr; }

	
}
