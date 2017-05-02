package de.fastr.phonegap.plugins;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaResourceApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.math.BigInteger;

import android.net.Uri;

public class md5chksum extends CordovaPlugin{

	public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
		if (action.equals("file")){
			String path = args.getString(0);
			this.runFile(path, callbackContext);
			return true;
		}
		return false;
	}

	private void runFile(final String path, final CallbackContext callbackContext){
		this.cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                file(path, callbackContext);
            }
        });
	}

  private boolean file(String path, CallbackContext callbackContext){
		//deal with cdvfile:// uris
		CordovaResourceApi resourceApi = webView.getResourceApi();
		Uri fileUri = Uri.parse(path); 
		fileUri = resourceApi.remapUri(fileUri.getScheme() != null ? fileUri: Uri.fromFile(new File(path)));

    File file = resourceApi.mapUriToFile(fileUri); 
    InputStream stream;
    MessageDigest md5;
    int bytesRead = 0;
    byte[] buf = new byte[4096];

    try{
      stream = new FileInputStream(file);

      md5 = MessageDigest.getInstance("md5");

      while((bytesRead = stream.read(buf)) > 0){
        md5.update(buf, 0, bytesRead);
      }
      stream.close();
      byte[] md5chksum = md5.digest();
      String hex = Base64.encodeToString(md5chksum, Base64.NO_WRAP);
      callbackContext.success(hex);
      return true;
    }catch (FileNotFoundException e){
      callbackContext.error("File not found" + fileUri.toString());
      return false;
    }catch (NoSuchAlgorithmException e){
      callbackContext.error("No MD5-Implementation Found");
      return false;
    }catch(IOException e){
      callbackContext.error("IO Error while processing MD5");
      return false;
    }
  }
}
