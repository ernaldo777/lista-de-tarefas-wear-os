package com.example.listadetarefas;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioDeviceCallback;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private AudioManager audioManager;
    private ArrayAdapter<String> adapter;
    private final ArrayList<String> itens = new ArrayList<>();

    private final AudioDeviceCallback audioDeviceCallback = new AudioDeviceCallback() {
        @Override
        public void onAudioDevicesAdded(AudioDeviceInfo[] addedDevices) {
            atualizarListaAudio();
        }

        @Override
        public void onAudioDevicesRemoved(AudioDeviceInfo[] removedDevices) {
            atualizarListaAudio();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        ListView listaAudio = findViewById(R.id.listaAudio);
        Button btnVerificar = findViewById(R.id.btnVerificar);
        Button btnBluetooth = findViewById(R.id.btnBluetooth);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, itens);
        listaAudio.setAdapter(adapter);

        btnVerificar.setOnClickListener(v -> atualizarListaAudio());

        btnBluetooth.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 100);
            }
        }

        audioManager.registerAudioDeviceCallback(audioDeviceCallback, null);
        atualizarListaAudio();
    }

    private void atualizarListaAudio() {
        itens.clear();

        boolean possuiSaidaAudio = getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT);

        itens.add("Assistente de áudio Wear OS");
        itens.add("Saída de áudio disponível: " + (possuiSaidaAudio ? "Sim" : "Não"));

        boolean altoFalante = audioOutputAvailable(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER);
        boolean bluetooth = audioOutputAvailable(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP);

        itens.add("Alto-falante interno: " + (altoFalante ? "Disponível" : "Não disponível"));
        itens.add("Fone Bluetooth: " + (bluetooth ? "Conectado" : "Não conectado"));

        AudioDeviceInfo[] dispositivos = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);

        if (dispositivos.length == 0) {
            itens.add("Nenhum dispositivo de saída encontrado.");
        } else {
            itens.add("Dispositivos detectados:");
            for (AudioDeviceInfo dispositivo : dispositivos) {
                itens.add("- Tipo: " + dispositivo.getType());
            }
        }

        Toast toast = Toast.makeText(this, "Verificação de áudio atualizada", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 20);
        toast.show();

        adapter.notifyDataSetChanged();
    }

    private boolean audioOutputAvailable(int type) {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT)) {
            return false;
        }

        AudioDeviceInfo[] devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);

        for (AudioDeviceInfo device : devices) {
            if (device.getType() == type) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        atualizarListaAudio();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (audioManager != null) {
            audioManager.unregisterAudioDeviceCallback(audioDeviceCallback);
        }
    }
}
