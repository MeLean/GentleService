package milen.com.gentleservice.services;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import milen.com.gentleservice.constants.ProjectConstants;
import milen.com.gentleservice.utils.SharedPreferencesUtils;

public class TokenService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        SharedPreferencesUtils.saveString(getApplicationContext(), ProjectConstants.SAVED_TOKEN, refreshedToken);
    }
}
