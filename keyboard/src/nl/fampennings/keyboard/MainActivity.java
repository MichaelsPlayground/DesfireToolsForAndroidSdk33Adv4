package nl.fampennings.keyboard;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

    CustomKeyboard mCustomKeyboard;
    
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mCustomKeyboard= new CustomKeyboard(this, R.id.keyboardview, R.xml.hexkbd );
        
        mCustomKeyboard.registerEditText(R.id.edittext0); 
        //mCustomKeyboard.registerEditText(R.id.edittext1);
        //mCustomKeyboard.registerEditText(R.id.edittext2);
        mCustomKeyboard.registerEditText(R.id.edittext3);
        mCustomKeyboard.registerEditText(R.id.edittext4);
    }
    
    @Override public void onBackPressed() { 
    	// NOTE Trap the back key: when the CustomKeyboard is still visible hide it, only when it is invisible, finish activity 
        if( mCustomKeyboard.isCustomKeyboardVisible() ) mCustomKeyboard.hideCustomKeyboard(); else this.finish();
    }

}

