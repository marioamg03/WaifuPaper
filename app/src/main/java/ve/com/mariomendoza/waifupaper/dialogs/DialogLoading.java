package ve.com.mariomendoza.waifupaper.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;

import com.airbnb.lottie.LottieAnimationView;
import ve.com.mariomendoza.waifupaper.R;

public class DialogLoading extends Dialog {

    private Context mContext;

    private LottieAnimationView lottieAnimationView;

    public DialogLoading(Context context) {
        super(context);
        this.mContext = context;

        Init();
    }

    private void Init () {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.dialog_loading);
        setTitle("");
        setCancelable(false);

        //Esto permite quitar el fondo blanco que aparece demas en la vista del dialogo cuando se
        //importa de la libreria android.app.AlertDialog

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        lottieAnimationView = findViewById(R.id.animation_loading);
        lottieAnimationView.setAnimation("wp-loading.json");
        lottieAnimationView.playAnimation();
    }

}
