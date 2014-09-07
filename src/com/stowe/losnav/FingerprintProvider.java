package com.stowe.losnav;

import java.util.List;

import com.db4o.query.Predicate;

import android.content.Context;
import android.util.Log;

public class FingerprintProvider extends Db4oHelper {

    public final static String TAG = "FingerprintProvider";
    private static FingerprintProvider provider = null;

    public FingerprintProvider(Context ctx) {
                  super(ctx);
    }

    public static FingerprintProvider getInstance(Context ctx) {
                  if (provider == null)
                                provider = new FingerprintProvider(ctx);
                  return provider;
    }

    public void store(Fingerprint fingerprint) {
                  db().store(fingerprint);
                  db().commit();
                  db().ext().purge();
    }
    
    public void dbPurge(){
    	db().ext().purge();
    }
    
    public void dbCommit(){
    	db().commit();
    }

    public void delete(Fingerprint fingerprint) {
                  db().delete(fingerprint);
    }

    public List<Fingerprint> findAll() {
                  return db().query(Fingerprint.class);
    }
    
    public void deleteAllFingerprints() {
    	Log.d(TAG, "deleteAllFingerprints() is being called!!!");
    	List<Fingerprint> fingerprints = db().query(Fingerprint.class);
    	for(Fingerprint fingerprint : fingerprints) {
    		db().delete(fingerprint);
    	}
    }
    
    public List<Fingerprint> findByLabel(String label) {
    	final String label1 = label;
    	List<Fingerprint> result = db().query(new Predicate<Fingerprint>() {
    		/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
    		public boolean match(Fingerprint o) {
    			return o._label.equals(label1);
    		}
    	});
    	
    	return result;
    }
    

}
