package ve.com.mariomendoza.waifupaper.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import ve.com.mariomendoza.waifupaper.R;


/**
 * @author Mario Mendoza
 * @version 1.0
 * @since 24-11-2019
 *
 * Dialogo para mostrar alguna ALERTA al usuario
 */
public class DialogAlert extends Dialog {

    private Dialog mThis;
    private Context mContext;

    ///////////////////////////////////
    ///                             ///
    ///         CONSTRUCTOR         ///
    ///                             ///
    ///////////////////////////////////

    public DialogAlert(Context context) {
        super(context);

        this.mThis = this;
        this.mContext = context;

        Init();
    }

    ///////////////////////////////////
    ///                             ///
    ///        INICIALIZADOR        ///
    ///                             ///
    ///////////////////////////////////

    private void Init () {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.dialog_alert);
        setTitle("");
        setCancelable(false);

        //Esto permite quitar el fondo blanco que aparece demas en la vista del dialogo cuando se
        //importa de la libreria android.app.AlertDialog

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        //Asignamos los eventos correspondientes

        eventOnClickButtonOk();
    }

    ///////////////////////////////////
    ///                             ///
    ///           EVENTOS           ///
    ///                             ///
    ///////////////////////////////////

    /**
     * Evento que se ejecuta al presionar el boton OK
     */
    private void eventOnClickButtonOk () {
        Button btnDialog = findViewById(R.id.btnOk);

        btnDialog.setOnClickListener(v -> {
            this.dismiss();
        });
    }

    ///////////////////////////////////
    ///                             ///
    ///           METHODS           ///
    ///                             ///
    ///////////////////////////////////

    /**
     * Agrega un texto al TextView utilizado como MENSAJE
     *
     * @param message String con el texto a colocar
     */
    public void setDialogMessage (String message) {
        TextView lblMessage = findViewById(R.id.lblMessage);
        lblMessage.setText(message);
    }

}