package org.otdshco;

import android.content.Context;
import android.content.SharedPreferences;

import android.os.Bundle;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import static org.otdshco.gauges.Params.defaultAccelerationFormulas;
import static org.otdshco.gauges.Params.defaultArrowDiameter;
import static org.otdshco.gauges.Params.defaultArrowMass;
import static org.otdshco.gauges.Params.defaultArrowVelocity;
import static org.otdshco.gauges.Params.defaultGravityFormulas;
import static org.otdshco.gauges.Params.defaultLensFactor;
import static org.otdshco.gauges.Params.defaultPersonHeight;
import static org.otdshco.gauges.Params.defaultSensorMaxSamples;

import static org.otdshco.gauges.Params.settingsAccelerationFormulas;
import static org.otdshco.gauges.Params.settingsArrowDiameter;
import static org.otdshco.gauges.Params.settingsArrowMass;
import static org.otdshco.gauges.Params.settingsArrowVelocity;
import static org.otdshco.gauges.Params.settingsEnvironmentValues;
import static org.otdshco.gauges.Params.settingsGravityFormulas;
import static org.otdshco.gauges.Params.settingsLensFactor;
import static org.otdshco.gauges.Params.settingsPersonHeight;
import static org.otdshco.gauges.Params.settingsSensorMaxSamples;

import androidx.appcompat.app.AppCompatActivity;

public class Settings extends AppCompatActivity
{
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.settings );
        loadSettings( );

        Button cancel_button = findViewById( R.id.cancel );
        cancel_button.setOnClickListener( v -> finish( ) );

        Button confirm_button = findViewById( R.id.confirm );
        confirm_button.setOnClickListener( v -> {
            saveSettings( );
            finish( );
        } );
    }

    private void saveSettings( )
    {
        SharedPreferences.Editor editor = getSharedPreferences( settingsEnvironmentValues, Context.MODE_PRIVATE ).edit( );

        EditText editTextSensorMaxSamples = findViewById( R.id.sensor_max_samples );
        editor.putInt( settingsSensorMaxSamples, Integer.parseInt( editTextSensorMaxSamples.getText( ).toString( ) ) );

        EditText editTextArrowVelocity = findViewById( R.id.arrow_velocity );
        editor.putFloat( settingsArrowVelocity, Float.parseFloat( editTextArrowVelocity.getText( ).toString( ) ) );

        EditText editTextPersonHeight = findViewById( R.id.person_height );
        editor.putFloat( settingsPersonHeight, Float.parseFloat( editTextPersonHeight.getText( ).toString( ) ) );

        EditText editTextArrowDiameter = findViewById( R.id.arrow_diameter );
        editor.putFloat( settingsArrowDiameter, Float.parseFloat( editTextArrowDiameter.getText( ).toString( ) ) );

        EditText editTextArrowMass = findViewById( R.id.arrow_mass );
        editor.putFloat( settingsArrowMass, Float.parseFloat( editTextArrowMass.getText( ).toString( ) ) );

        Switch switchAcceleration = findViewById( R.id.acceleration_formulas );
        editor.putBoolean( settingsAccelerationFormulas, switchAcceleration.isChecked( ) );

        Switch switchGravity = findViewById( R.id.gravity_formulas );
        editor.putBoolean( settingsGravityFormulas, switchGravity.isChecked( ) );

        EditText editTextLensFactor = findViewById( R.id.lens_factor );
        editor.putInt( settingsLensFactor, Integer.parseInt( editTextLensFactor.getText( ).toString( ) ) );

        editor.apply( );
    }

    private void loadSettings( )
    {
        SharedPreferences settings = getSharedPreferences( settingsEnvironmentValues, MODE_PRIVATE );

        EditText editTextSensorMaxSamples = findViewById( R.id.sensor_max_samples );
        editTextSensorMaxSamples.setText( String.valueOf( settings.getInt( settingsSensorMaxSamples, defaultSensorMaxSamples ) ) );

        EditText editTextArrowVelocity = findViewById( R.id.arrow_velocity );
        editTextArrowVelocity.setText( String.valueOf( settings.getFloat( settingsArrowVelocity, defaultArrowVelocity ) ) );

        EditText editTextPersonHeight = findViewById( R.id.person_height );
        editTextPersonHeight.setText( String.valueOf( settings.getFloat( settingsPersonHeight, defaultPersonHeight ) ) );

        EditText editTextArrowDiameter = findViewById( R.id.arrow_diameter );
        editTextArrowDiameter.setText( String.valueOf( settings.getFloat( settingsArrowDiameter, defaultArrowDiameter ) ) );

        EditText editTextArrowMass = findViewById( R.id.arrow_mass );
        editTextArrowMass.setText( String.valueOf( settings.getFloat( settingsArrowMass, defaultArrowMass ) ) );

        Switch editTextAccelerationFormulas = findViewById( R.id.acceleration_formulas );
        editTextAccelerationFormulas.setChecked( settings.getBoolean( settingsAccelerationFormulas, defaultAccelerationFormulas ) );

        Switch editTextGravityFormulas = findViewById( R.id.gravity_formulas );
        editTextGravityFormulas.setChecked( settings.getBoolean( settingsGravityFormulas, defaultGravityFormulas ) );

        EditText editTextLensFactor = findViewById( R.id.lens_factor );
        editTextLensFactor.setText( String.valueOf( settings.getInt( settingsLensFactor, defaultLensFactor ) ) );
    }
}