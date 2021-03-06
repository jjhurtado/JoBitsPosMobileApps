package com.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.Toast;

import com.controllers.CentroElaboracionController;
import com.services.models.InsumoAlmacenModel;
import com.utils.adapters.CentroElaboracionAdapter;
import com.utils.adapters.CentroElaboracionRecetaAdapter;
import com.utils.adapters.SelecElaboracionAdapter;
import com.utils.exception.ExceptionHandler;
import com.utils.loading.LoadingHandler;
import com.utils.loading.LoadingProcess;

import java.util.ArrayList;
import java.util.List;

/**
 * Capa: Activities
 * Clase que controla el XML de la pantalla principal del Punto de Elaboracion.
 *
 * @extends BaseActivity ya que es una activity propia de la aplicacion.
 */
public class CentroElaboracionActivity extends BaseActivity {

    private ListView listViewIngredientes;
    private ListView listViewReceta;
    private ListView listViewSelecIngrediente;

    private Button buttonAgregarIngrediente;
    private Button buttonAgregarReceta;
    private Button buttonConfirmar;

    private TabHost host;
    private CentroElaboracionAdapter ingredientesAdapter;
    private CentroElaboracionRecetaAdapter recetaAdapter;
    private SelecElaboracionAdapter selecElaboracionAdapter;

    private CentroElaboracionController controller;
    private List<InsumoAlmacenModel> listaInsumosIngrediente;
    private List<InsumoAlmacenModel> listaInsumosReceta;
    private boolean isReceta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_centro_elaboracion);

            initVarialbes();//inicializa las  variables
            addListeners();//agrega los listener
            setAdapters();//agrega los adapters
        } catch (Exception e) {
            ExceptionHandler.handleException(e, act);
        }
    }

    @Override
    void initVarialbes() {
        try {
            listViewIngredientes = (ListView) findViewById(R.id.listViewIngrediente);
            listViewReceta = (ListView) findViewById(R.id.listViewReceta);
            listViewSelecIngrediente = (ListView) findViewById(R.id.listViewSelecIng);

            buttonAgregarIngrediente = (Button) findViewById(R.id.buttonAgregarIngrediente);
            buttonAgregarReceta = (Button) findViewById(R.id.buttonAgregarReceta);
            buttonConfirmar = (Button) findViewById(R.id.buttonConfirmar);

            controller = new CentroElaboracionController();
            listaInsumosIngrediente = new ArrayList<InsumoAlmacenModel>();
            listaInsumosReceta = new ArrayList<InsumoAlmacenModel>();
            isReceta = false;

            initTab();
        } catch (Exception e) {
            ExceptionHandler.handleException(e, act);
        }
    }

    @Override
    void addListeners() {
        buttonAgregarIngrediente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listaInsumosIngrediente.size() != 0) {
                    Toast.makeText(getApplicationContext(), "Cantidad màxima de productos alcanzada", Toast.LENGTH_SHORT).show();
                } else {
                    getProductosDisponibles();
                    host.setCurrentTab(1);
                    isReceta = false;
                }
            }
        });
        buttonAgregarReceta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCombinacionesCon(listaInsumosIngrediente);
                host.setCurrentTab(1);
                isReceta = true;
            }
        });
        listViewSelecIngrediente.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                addProductosCant(1, position);
                if (isReceta == false) {
                    setListProductSelec();
                } else {
                    setListRecetaSelec();
                }
                host.setCurrentTab(0);
            }
        });
        listViewSelecIngrediente.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return onListViewElabAddLongClick(view, position);
            }
        });

        buttonConfirmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonConfirmarClick();
            }
        });
    }

    private void onButtonTerminarClick() {
        if (isReceta == false) {
            setListProductSelec();
        } else {
            setListRecetaSelec();
        }
        host.setCurrentTab(0);
    }

    private void onButtonConfirmarClick() {
        new LoadingHandler<Void>(act, new LoadingProcess<Void>() {
            @Override
            public Void process() throws Exception {
                controller.transformar(listaInsumosIngrediente, listaInsumosReceta);
                return null;
            }

            @Override
            public void post(Void answer) {
                listViewSelecIngrediente.setAdapter(selecElaboracionAdapter);
                finish();
            }
        });
    }

    private boolean onListViewElabAddLongClick(final View v, final int position) {
        try {
            final EditText input = new EditText(v.getContext());
            input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
            input.setRawInputType(Configuration.KEYBOARD_12KEY);
            new AlertDialog.Builder(v.getContext()).
                    setView(input).
                    setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).setPositiveButton(R.string.agregar, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    addProductosCant(Float.parseFloat(input.getText().toString()), position);
                }
            }).create().show();
            if (isReceta == false) {
                setListProductSelec();
            } else {
                setListRecetaSelec();
            }
            host.setCurrentTab(0);
            return true;
        } catch (Exception e) {
            ExceptionHandler.handleException(e, act);
            return false;
        }
    }

    private void addProductosCant(float cant, int position) {
        InsumoAlmacenModel insumo = (InsumoAlmacenModel) listViewSelecIngrediente.getItemAtPosition(position);
        if (isReceta == false) {
            if (listaInsumosIngrediente.contains(insumo)) {
                for (int i = 0; i < listaInsumosIngrediente.size(); i++) {
                    if (listaInsumosIngrediente.get(i).equals(insumo)) {
                        listaInsumosIngrediente.get(i).setCantidad(listaInsumosIngrediente.get(i).getCantidad() + cant);
                    }
                }
            } else {
                insumo.setCantidad(insumo.getCantidad() + cant);
                listaInsumosIngrediente.add(insumo);
            }
        } else if (isReceta == true) {
            if (listaInsumosReceta.contains(insumo)) {
                for (int i = 0; i < listaInsumosReceta.size(); i++) {
                    if (listaInsumosReceta.get(i).equals(insumo)) {
                        listaInsumosReceta.get(i).setCantidad(listaInsumosReceta.get(i).getCantidad() + cant);
                    }
                }
            } else {
                insumo.setCantidad(insumo.getCantidad() + cant);
                listaInsumosReceta.add(insumo);
            }
        }
        Toast.makeText(getApplicationContext(), "Producto agregado.", Toast.LENGTH_SHORT).show();
    }

    private void getProductosDisponibles() {
        try {
            new LoadingHandler<Void>(act, new LoadingProcess<Void>() {
                @Override
                public Void process() throws Exception {
                    selecElaboracionAdapter = new SelecElaboracionAdapter(act, R.layout.list_selec_elab, controller.getProductosDisponibles());
                    return null;
                }

                @Override
                public void post(Void answer) {
                    listViewSelecIngrediente.setAdapter(selecElaboracionAdapter);
                }
            });
        } catch (Exception e) {
            ExceptionHandler.handleException(e, act);
        }
    }

    private void getCombinacionesCon(final List<InsumoAlmacenModel> lista) {
        try {
            new LoadingHandler<Void>(act, new LoadingProcess<Void>() {
                @Override
                public Void process() throws Exception {
                    selecElaboracionAdapter = new SelecElaboracionAdapter(act, R.layout.list_selec_elab, controller.getCombinacionesCon(lista));
                    return null;
                }

                @Override
                public void post(Void answer) {
                    listViewSelecIngrediente.setAdapter(selecElaboracionAdapter);
                }
            });
        } catch (Exception e) {
            ExceptionHandler.handleException(e, act);
        }
    }

    private void setListProductSelec() {
        try {
            new LoadingHandler<Void>(act, new LoadingProcess<Void>() {
                @Override
                public Void process() throws Exception {
                    ingredientesAdapter = new CentroElaboracionAdapter(act, R.layout.list_elaboracion, listaInsumosIngrediente);
                    ingredientesAdapter.setAddListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            final InsumoAlmacenModel insumoAlmacenModel = ((InsumoAlmacenModel) listViewIngredientes.getAdapter().getItem((Integer) v.getTag()));
                            final EditText input = new EditText(v.getContext());
                            input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                            input.setRawInputType(Configuration.KEYBOARD_12KEY);
                            new AlertDialog.Builder(v.getContext()).
                                    setView(input).
                                    setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).setPositiveButton(R.string.agregar, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    addProducto(insumoAlmacenModel, Float.parseFloat(input.getText().toString()));
                                }
                            }).create().show();
                            return true;
                        }
                    });
                    ingredientesAdapter.setRemoveListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            final InsumoAlmacenModel insumoAlmacenModel = ((InsumoAlmacenModel) listViewIngredientes.getAdapter().getItem((Integer) v.getTag()));
                            final EditText input = new EditText(v.getContext());
                            input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                            input.setRawInputType(Configuration.KEYBOARD_12KEY);
                            new AlertDialog.Builder(v.getContext()).
                                    setView(input).
                                    setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).setPositiveButton(R.string.agregar, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    removeProducto(insumoAlmacenModel, Float.parseFloat(input.getText().toString()));
                                }
                            }).create().show();
                            return true;
                        }
                    });
                    return null;
                }

                @Override
                public void post(Void answer) {
                    listViewIngredientes.setAdapter(ingredientesAdapter);
                }
            });
        } catch (Exception e) {
            ExceptionHandler.handleException(e, act);
        }
    }

    private void setListRecetaSelec() {
        try {
            new LoadingHandler<Void>(act, new LoadingProcess<Void>() {
                @Override
                public Void process() throws Exception {
                    recetaAdapter = new CentroElaboracionRecetaAdapter(act, R.layout.list_elaboracion_receta, listaInsumosReceta);
                    recetaAdapter.setAddListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            final InsumoAlmacenModel insumoAlmacenModel = ((InsumoAlmacenModel) listViewReceta.getAdapter().getItem((Integer) v.getTag()));
                            final EditText input = new EditText(v.getContext());
                            input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                            input.setRawInputType(Configuration.KEYBOARD_12KEY);
                            new AlertDialog.Builder(v.getContext()).
                                    setView(input).
                                    setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).setPositiveButton(R.string.agregar, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    addReceta(insumoAlmacenModel, Float.parseFloat(input.getText().toString()));
                                }
                            }).create().show();
                            return true;
                        }
                    });
                    recetaAdapter.setRemoveListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            final InsumoAlmacenModel insumoAlmacenModel = ((InsumoAlmacenModel) listViewReceta.getAdapter().getItem((Integer) v.getTag()));
                            final EditText input = new EditText(v.getContext());
                            input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                            input.setRawInputType(Configuration.KEYBOARD_12KEY);
                            new AlertDialog.Builder(v.getContext()).
                                    setView(input).
                                    setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).setPositiveButton(R.string.agregar, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    removeReceta(insumoAlmacenModel, Float.parseFloat(input.getText().toString()));
                                }
                            }).create().show();
                            return true;
                        }
                    });

                    return null;
                }

                @Override
                public void post(Void answer) {
                    listViewReceta.setAdapter(recetaAdapter);
                }
            });
        } catch (Exception e) {
            ExceptionHandler.handleException(e, act);
        }
    }

    private void initTab() {
        try {
            host = (TabHost) findViewById(R.id.tabHost);
            if (host != null) {//TODO: por que este if??
                host.setup();

                TabHost.TabSpec spec = host.newTabSpec("Centro");

                //Tab 1
                spec.setContent(R.id.tab1);
                spec.setIndicator("Centro");
                host.addTab(spec);

                //Tab 2
                spec = host.newTabSpec("List");
                spec.setContent(R.id.tab2);
                spec.setIndicator("List");
                host.addTab(spec);
            }
        } catch (Exception e) {
            ExceptionHandler.handleException(e, this);
        }
    }

    public void onAddProductoClick(View v) {
        try {
            InsumoAlmacenModel insumoAlmacenModel = ((InsumoAlmacenModel) listViewIngredientes.getAdapter().getItem((Integer) v.getTag()));
            addProducto(insumoAlmacenModel, 1);
        } catch (Exception e) {
            ExceptionHandler.handleException(e, act);
        }
    }

    private void addProducto(InsumoAlmacenModel insumoAlmacenModel, float cant) {
        for (int i = 0; i < listaInsumosIngrediente.size(); i++) {
            if (listaInsumosIngrediente.get(i).equals(insumoAlmacenModel)) {
                listaInsumosIngrediente.get(i).setCantidad(listaInsumosIngrediente.get(i).getCantidad() + cant);
            }
        }
        setListProductSelec();
    }

    public void onRemoveProductoClick(View v) {
        try {
            InsumoAlmacenModel insumoAlmacenModel = ((InsumoAlmacenModel) listViewIngredientes.getAdapter().getItem((Integer) v.getTag()));
            removeProducto(insumoAlmacenModel, 1);
        } catch (Exception e) {
            ExceptionHandler.handleException(e, act);
        }
    }

    private void removeProducto(InsumoAlmacenModel insumoAlmacenModel, float cant) {
        for (int i = 0; i < listaInsumosIngrediente.size(); i++) {
            if (listaInsumosIngrediente.get(i).equals(insumoAlmacenModel)) {
                if (listaInsumosIngrediente.get(i).getCantidad() <= 1 || listaInsumosIngrediente.get(i).getCantidad() - cant <= 0) {
                    listaInsumosIngrediente.remove(i);
                    Toast.makeText(getApplicationContext(), "Producto eliminado.", Toast.LENGTH_SHORT).show();
                } else {
                    listaInsumosIngrediente.get(i).setCantidad(listaInsumosIngrediente.get(i).getCantidad() - cant);
                }
            }
        }
        setListProductSelec();
    }

    public void onAddRecetaClick(View v) {
        try {
            InsumoAlmacenModel insumoAlmacenModel = ((InsumoAlmacenModel) listViewReceta.getAdapter().getItem((Integer) v.getTag()));
            addReceta(insumoAlmacenModel, 1);
        } catch (Exception e) {
            ExceptionHandler.handleException(e, act);
        }
    }

    private void addReceta(InsumoAlmacenModel insumoAlmacenModel, float cant) {
        for (int i = 0; i < listaInsumosReceta.size(); i++) {
            if (listaInsumosReceta.get(i).equals(insumoAlmacenModel)) {
                listaInsumosReceta.get(i).setCantidad(listaInsumosReceta.get(i).getCantidad() + cant);
            }
        }
        setListRecetaSelec();
    }

    public void onRemoveRecetaClick(View v) {
        try {
            InsumoAlmacenModel insumoAlmacenModel = ((InsumoAlmacenModel) listViewReceta.getAdapter().getItem((Integer) v.getTag()));
            removeReceta(insumoAlmacenModel, 1);
        } catch (Exception e) {
            ExceptionHandler.handleException(e, act);
        }
    }

    private void removeReceta(InsumoAlmacenModel insumoAlmacenModel, float cant) {
        for (int i = 0; i < listaInsumosReceta.size(); i++) {
            if (listaInsumosReceta.get(i).equals(insumoAlmacenModel)) {
                if (listaInsumosReceta.get(i).getCantidad() <= 1 || listaInsumosReceta.get(i).getCantidad() - cant <= 0) {
                    listaInsumosReceta.remove(i);
                    Toast.makeText(getApplicationContext(), "Producto eliminado.", Toast.LENGTH_SHORT).show();
                } else {
                    listaInsumosReceta.get(i).setCantidad(listaInsumosReceta.get(i).getCantidad() - cant);
                }
            }
        }
        setListRecetaSelec();
    }

    @Override
    public void onBackPressed() {
        if (host.getCurrentTab() == 0) {
            super.onBackPressed();
        } else {
            host.setCurrentTab(0);
        }
    }
}
