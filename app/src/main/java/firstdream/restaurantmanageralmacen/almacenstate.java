package firstdream.restaurantmanageralmacen;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.main.InsumoAlmacen;
import com.main.adapters.adapterAlmacenInsumo;
import com.main.webServerCon.almacenConn;

public class almacenstate extends BaseActivity {

    ListView lista;
    TextView usuario, almacen;
    EditText searchText;
    almacenConn conn;
    RadioButton radioButtonSalida, radioButtonRebaja;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_almacenstate);
        initComponents(getIntent().getExtras());


    }

    private void initComponents(Bundle extras) {
        lista = (ListView) findViewById(R.id.listaInsumos);
        usuario = (TextView) findViewById(R.id.textUser);
        almacen = (TextView) findViewById(R.id.textViewNombreAlmacen);
        usuario.setText(extras.getString(String.valueOf(R.string.user)));
        searchText = (EditText) findViewById(R.id.editText);
        conn = new almacenConn(usuario.getText().toString(), null);
        radioButtonSalida = (RadioButton) findViewById(R.id.radioButtonSalida);
        radioButtonRebaja = (RadioButton) findViewById(R.id.radioButtonRebaja);


        lista.setAdapter(getData());


        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ((adapterAlmacenInsumo) lista.getAdapter()).getFilter().filter(s.toString());

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void logicDarEntrada(final View v) {
        final InsumoAlmacen i = ((InsumoAlmacen) lista.getAdapter().getItem((Integer) v.getTag()));

        final EditText input = new EditText(v.getContext());
        final EditText amount = new EditText(v.getContext());
        input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setRawInputType(Configuration.KEYBOARD_12KEY);
        new AlertDialog.Builder(v.getContext()).
                setView(input).
                setTitle("Entrada de Insumo").
                setMessage("Introduzca la cantidad de " + i.getInsumo() + " a dar entrada").
                setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setPositiveButton(R.string.agregar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new AlertDialog.Builder(v.getContext()).setView(amount).setTitle("Monto").
                        setMessage("Introduzca el valor de la entrada").setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        float cantidad = 0,monto = 0;
                        try{
                            cantidad = Float.parseFloat(input.getText().toString());
                            monto = Float.parseFloat(amount.getText().toString());
                        }
                        catch (Exception e){
                            dialog.dismiss();
                            return;
                        }

                        conn.darEntrada(i, cantidad, monto);
                        lista.post(new Runnable() {
                            @Override
                            public void run() {
                                lista.setAdapter(getData());
                            }
                        });
                        dialog.dismiss();
                    }
                }).create().show();
                dialog.dismiss();

            }
        }).
                create().
                show();
    }

    public void logicRebajar(View v) {
        if (radioButtonSalida.isChecked()) {
            logicDarSalida(v);
        } else {
            logicDarRebaja(v);
        }
    }

    private void logicDarSalida(final View v) {
        final InsumoAlmacen i = ((InsumoAlmacen) lista.getAdapter().getItem((Integer) v.getTag()));

        final EditText input = new EditText(v.getContext());
        final String[] ipvs = getIPVData(i.getInsumo().getCodInsumo());
        input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setRawInputType(Configuration.KEYBOARD_12KEY);
        new AlertDialog.Builder(v.getContext()).
                setView(input).
                setTitle("Salida a punto de elaboracion").
                setMessage("Introduzca la cantidad de " + i.getInsumo() + " a dar salida").
                setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).
                setPositiveButton(R.string.agregar, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new AlertDialog.Builder(v.getContext())
                                .setItems(ipvs, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        float cantidad = 0;
                                        try{
                                        cantidad = Float.parseFloat(input.getText().toString());}
                                        catch (Exception e){
                                            dialog.dismiss();
                                        }
                                        if(cantidad > i.getCantidad()){
                                            Toast.makeText(v.getContext(),R.string.saldo_insuficiente,Toast.LENGTH_LONG);
                                            dialog.dismiss();
                                        }else{
                                        conn.darSalida(i, cantidad, ipvs[which]);
                                        lista.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                lista.setAdapter(getData());
                                            }
                                        });
                                        dialog.dismiss();}
                                    }
                                }).
                                setTitle("Destino").
                                setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).
                                create().
                                show();
                        dialog.dismiss();

                    }
                }).
                create().
                show();
    }

    private void logicDarRebaja(final View v) {
        final InsumoAlmacen i = ((InsumoAlmacen) lista.getAdapter().getItem((Integer) v.getTag()));

        final EditText input = new EditText(v.getContext());
        input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setRawInputType(Configuration.KEYBOARD_12KEY);
        final EditText razon = new EditText(v.getContext());
        razon.setInputType(InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
        razon.setRawInputType(Configuration.KEYBOARD_QWERTY);
        new AlertDialog.Builder(v.getContext()).
                setView(input).
                setTitle("Merma").
                setMessage("Introduzca la cantidad de " + i.getInsumo() + " a rebajar").
                setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).
                setPositiveButton(R.string.agregar, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new AlertDialog.Builder(v.getContext()).
                                setView(razon).
                                setTitle("Razon de rebaja").
                                setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).setPositiveButton("Mermar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                float cantidad = 0;
                                try{
                                    cantidad = Float.parseFloat(input.getText().toString());}
                                catch (Exception e){
                                    dialog.dismiss();
                                }
                                if(cantidad > i.getCantidad()){
                                    Toast.makeText(v.getContext(),R.string.saldo_insuficiente,Toast.LENGTH_LONG);
                                    dialog.dismiss();
                                }else{
                                    conn.rebajar(i, cantidad, razon.getText().toString());
                                    lista.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            lista.setAdapter(getData());
                                        }
                                    });
                                    dialog.dismiss();}
                            }

                        }).
                                create().
                                show();
                        dialog.dismiss();

                    }
                }).
                create().
                show();
    }

    private adapterAlmacenInsumo getData() {
        return new adapterAlmacenInsumo(this, R.id.listaInsumos, conn.getPrimerAlmacen());
    }

    private String[] getIPVData(String insumoCod) {
        return conn.getCocinasNamesForIPV(insumoCod);
    }

}
