package io.kobold;

public final class Config {
	
	//true = atena old, false = MD
	public final String buildType;
	public final String atenaMdFolder;
	public final String appFolderName;
	public final String appName;
	public final String packageName;
	public final String clientId;
	public final String clientSecret;
	public final String prodUrl;
	public final String qaUrl;
	public final String imagesFolder;
	public final String configFilesFolder;
	
	//MD Fields
	public final String fcmSenderId;
	public final String pushwoodAppId;
	
	//Old Atena optional to MD
	public final String appKey;
	public final String keyAlias;
	public final String appKeyIdentifier;
	
	public Config(String[] args) {
		buildType = args[0];
		atenaMdFolder = args[1];
		appFolderName = args[2];
		appName = args[3];
		packageName = args[4];
		clientId = args[5];
		clientSecret = args[6];
		prodUrl = args[7];
		qaUrl = args[8];
		imagesFolder = args[9];
		configFilesFolder = args[10];
		
		//MD
		if(buildType.equals("false")) {
			fcmSenderId = args[11];
			pushwoodAppId = args[12];
			appKey = null;
			keyAlias = null;
			appKeyIdentifier = null;
		}
		/*Atena old*/
		else {
			fcmSenderId = null;
			pushwoodAppId = null;
			appKey = args[11];
			keyAlias = args[12];
			appKeyIdentifier = args[13];
		}
	}
}
