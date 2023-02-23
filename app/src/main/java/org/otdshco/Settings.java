package org.otdshco;

import android.content.Context;
import android.content.SharedPreferences;

import android.os.Bundle;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import org.otdshco.gauges.Params;

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
        SharedPreferences.Editor editor = getSharedPreferences( Params.settingsEnvironmentValues, Context.MODE_PRIVATE ).edit( );

        EditText editTextSensorMaxSamples = findViewById( R.id.sensor_max_samples );
        editor.putInt( Params.settingsSensorMaxSamples, Integer.parseInt( editTextSensorMaxSamples.getText( ).toString( ) ) );

        EditText editTextArrowVelocity = findViewById( R.id.arrow_velocity );
        editor.putFloat( Params.settingsArrowVelocity, Float.parseFloat( editTextArrowVelocity.getText( ).toString( ) ) );

        EditText editTextPersonHeight = findViewById( R.id.person_height );
        editor.putFloat( Params.settingsPersonHeight, Float.parseFloat( editTextPersonHeight.getText( ).toString( ) ) );

        EditText editTextArrowDiameter = findViewById( R.id.arrow_diameter );
        editor.putFloat( Params.settingsArrowDiameter, Float.parseFloat( editTextArrowDiameter.getText( ).toString( ) ) );

        EditText editTextArrowMass = findViewById( R.id.arrow_mass );
        editor.putFloat( Params.settingsArrowMass, Float.parseFloat( editTextArrowMass.getText( ).toString( ) ) );

        Switch switchAcceleration = findViewById( R.id.acceleration_formulas );
        editor.putBoolean( Params.settingsAccelerationFormulas, switchAcceleration.isChecked( ) );

        Switch switchGravity = findViewById( R.id.gravity_formulas );
        editor.putBoolean( Params.settingsGravityFormulas, switchGravity.isChecked( ) );

        Switch switchMethod = findViewById( R.id.sensor_method );
        editor.putBoolean( Params.settingsSensorMethod, switchMethod.isChecked( ) );

        EditText editTextLensFactor = findViewById( R.id.lens_factor );
        editor.putFloat( Params.settingsLensFactor, Float.parseFloat( editTextLensFactor.getText( ).toString( ) ) );

        editor.apply( );
    }

    private void loadSettings( )
    {
        SharedPreferences settings = getSharedPreferences( Params.settingsEnvironmentValues, MODE_PRIVATE );

        EditText editTextSensorMaxSamples = findViewById( R.id.sensor_max_samples );
        editTextSensorMaxSamples.setText( String.valueOf( settings.getInt( Params.settingsSensorMaxSamples, Params.defaultSensorMaxSamples ) ) );

        EditText editTextArrowVelocity = findViewById( R.id.arrow_velocity );
        editTextArrowVelocity.setText( String.valueOf( settings.getFloat( Params.settingsArrowVelocity, Params.defaultArrowVelocity ) ) );

        EditText editTextPersonHeight = findViewById( R.id.person_height );
        editTextPersonHeight.setText( String.valueOf( settings.getFloat( Params.settingsPersonHeight, Params.defaultPersonHeight ) ) );

        EditText editTextArrowDiameter = findViewById( R.id.arrow_diameter );
        editTextArrowDiameter.setText( String.valueOf( settings.getFloat( Params.settingsArrowDiameter, Params.defaultArrowDiameter ) ) );

        EditText editTextArrowMass = findViewById( R.id.arrow_mass );
        editTextArrowMass.setText( String.valueOf( settings.getFloat( Params.settingsArrowMass, Params.defaultArrowMass ) ) );

        Switch editTextAccelerationFormulas = findViewById( R.id.acceleration_formulas );
        editTextAccelerationFormulas.setChecked( settings.getBoolean( Params.settingsAccelerationFormulas, Params.defaultAccelerationFormulas ) );

        Switch editTextGravityFormulas = findViewById( R.id.gravity_formulas );
        editTextGravityFormulas.setChecked( settings.getBoolean( Params.settingsGravityFormulas, Params.defaultGravityFormulas ) );

        Switch editTextSensorMethod = findViewById( R.id.sensor_method );
        editTextSensorMethod.setChecked( settings.getBoolean( Params.settingsSensorMethod, Params.defaultSensorMethod ) );

        EditText editTextLensFactor = findViewById( R.id.lens_factor );
        editTextLensFactor.setText( String.valueOf( settings.getFloat( Params.settingsLensFactor, Params.defaultLensFactor ) ) );
    }
}