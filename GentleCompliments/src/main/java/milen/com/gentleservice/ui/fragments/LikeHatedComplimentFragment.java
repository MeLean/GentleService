package milen.com.gentleservice.ui.fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import milen.com.gentleservice.R;
import milen.com.gentleservice.api.database.DBHelper;
import milen.com.gentleservice.api.objects_model.Compliment;
import milen.com.gentleservice.ui.adapters.CustomAdapter;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class LikeHatedComplimentFragment extends Fragment implements AdapterView.OnItemClickListener {
    private ArrayList<String> mHatedComplimentsStr;
    private Activity mActivity;

    public LikeHatedComplimentFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_like_hated_compliment, container, false);
        mActivity = getActivity();

        DBHelper db = DBHelper.getInstance(mActivity);
        ArrayList<Compliment> hatedCompliments = new ArrayList<>();

        try {
            hatedCompliments = db.getHeatedComplements();
        } catch (ParseException | SQLException e) {
            e.printStackTrace();
        }


        mHatedComplimentsStr = new ArrayList<>();
        for (Compliment compliment : hatedCompliments) {
            mHatedComplimentsStr.add(compliment.getContent());
        }

        loadList(mHatedComplimentsStr, rootView);
        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
        final String complimentText = parent.getItemAtPosition(position).toString();

        //show dialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);
        // set title
        alertDialogBuilder.setTitle(getString(R.string.like_hated_title));
        // set dialog message
        alertDialogBuilder
                .setMessage(getString(R.string.like_hated_message) + "\"" + complimentText + "\"")
                .setCancelable(false)
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        removeComplimentFromList(view, complimentText, false);
                    }
                })
                .setNeutralButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        removeComplimentFromList(view, complimentText, true);
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
        ;
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void loadList(ArrayList<String> hatedComplimentsStr,View view) {
        ListView listView = view.findViewById(R.id.lv_like_hated);

        if (hatedComplimentsStr.size() > 0) {
            listView.setOnItemClickListener(this);
        } else {
            hatedComplimentsStr.add(getString(R.string.no_hated_compliments_text));
            listView.setOnItemClickListener(null);
        }

        ArrayAdapter<String> adapter = new CustomAdapter(mActivity, hatedComplimentsStr);
        listView.setAdapter(adapter);
    }

    private void removeComplimentFromList(View view, String complimentText, boolean isItForDelete) {
        Animation fadeOut = AnimationUtils.loadAnimation(mActivity, android.R.anim.fade_out);
        view.setAnimation(fadeOut);
        view.setVisibility(View.GONE);

        DBHelper db = DBHelper.getInstance(mActivity);
        try {
            if (isItForDelete) {
                db.deleteCompliment(complimentText);
            } else {
                db.changeIsHatedStatus(complimentText, false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String msg = isItForDelete ? getString(R.string.hated_was_deleted) : getString(R.string.hated_was_removed);
        Toast toast = Toast.makeText(mActivity, String.format("\"%s\" %s", complimentText, msg), Toast.LENGTH_SHORT);
        TextView textView = toast.getView().findViewById(android.R.id.message);
        if (textView != null) {
            textView.setGravity(Gravity.CENTER);
        }
        toast.show();
        mHatedComplimentsStr.remove(complimentText);
        loadList(mHatedComplimentsStr, getView());
    }
}
