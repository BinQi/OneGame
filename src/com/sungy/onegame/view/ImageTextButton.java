package com.sungy.onegame.view;

import com.sungy.onegame.R;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ImageTextButton extends RelativeLayout {
    
    private ImageView imgView;  
    private TextView  textView;
    private Typeface tf;
    
    public ImageTextButton(Context context) {
        super(context,null);
    }
    
    public ImageTextButton(Context context,AttributeSet attributeSet) {
        super(context, attributeSet);
        
        LayoutInflater.from(context).inflate(R.layout.img_text_bt, this,true);
        AssetManager mgr = context.getAssets();//得到AssetManager
        tf = Typeface.createFromAsset(mgr, "fonts/font.ttf");//根据路径得到Typeface
        this.imgView = (ImageView)findViewById(R.id.img_bt_img);
        this.textView = (TextView)findViewById(R.id.img_bt_text);
        this.textView.setTypeface(tf);
        
        this.setClickable(true);
        this.setFocusable(true);     
    }
    
    public void setImgResource(int resourceID) {
        this.imgView.setImageResource(resourceID);
    }
    
    public void setText(String text) {
        this.textView.setText(text);
    }
    
    public void setTextColor(int color) {
        this.textView.setTextColor(color);
    }
    
    public void setTextSize(float size) {
        this.textView.setTextSize(size);
    } 
}