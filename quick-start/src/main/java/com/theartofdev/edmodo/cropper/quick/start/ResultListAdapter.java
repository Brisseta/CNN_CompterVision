package com.theartofdev.edmodo.cropper.quick.start;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Iterator;

public final class ResultListAdapter extends BaseAdapter {

    private final Context mContext;
    private final ImageLoader mVolleyImageLoader;
    private JSONArray mResults;

    public ResultListAdapter(Context context, ImageLoader imageLoader) {
        mContext = context;
        mVolleyImageLoader = imageLoader;
    }

    public void updateMembers(JSONObject result) throws JSONException {
        mResults = result.getJSONArray("urls");
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return (mResults == null) ? 0 : mResults.length();
    }

    @Override
    public JSONObject getItem(int position) {
        JSONObject item = null;
        if (mResults != null) {
            try {
                item = mResults.getJSONObject(position);
            } catch (JSONException e) {
                // loguer l'erreur
            }
        }
        return item;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView login;
        NetworkImageView avatar;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.activity_result, parent, false);
            login = (TextView) convertView.findViewById(R.id.login);
            avatar = (NetworkImageView) convertView.findViewById(R.id.avatar);
            convertView.setTag(new ViewHolder(login, avatar));
        } else {
            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            login = viewHolder.mScore;
            avatar = viewHolder.mAvatar;
        }

        // On récupère les informations depuis le JSONObject et on les relie aux vues
        JSONObject json = getItem(position);
        float f =round(Float.parseFloat(json.optString("score")),Config.ROUND_NB);
        String s = Float.toString(f);
        login.setText(s);
        avatar.setImageUrl("http://" + json.optString("result_lib"), mVolleyImageLoader);
        return convertView;
    }

    static final class ViewHolder {
        final TextView mScore;
        final NetworkImageView mAvatar;

        public ViewHolder(TextView score, NetworkImageView avatar) {
            mScore = score;
            mAvatar = avatar;
        }
    }
    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }
}