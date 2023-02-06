package org.otdshco;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

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
        SharedPreferences sharedPref = getSharedPreferences( "environment_settings", Context.MODE_PRIVATE );
        SharedPreferences.Editor editor = sharedPref.edit( );

        EditText edit_text_sensor_max_samples = findViewById( R.id.sensor_max_samples );
        String edit_text_value1 = edit_text_sensor_max_samples.getText( ).toString( );
        int sensor_max_samples_value = Integer.parseInt( edit_text_value1 );
        editor.putInt( "sensor_max_samples", sensor_max_samples_value );

        EditText edit_text_arrow_velocity = findViewById( R.id.arrow_velocity );
        String edit_text_value2 = edit_text_arrow_velocity.getText( ).toString( );
        float arrow_velocity_value = Float.parseFloat( edit_text_value2 );
        editor.putFloat( "arrow_velocity", arrow_velocity_value );

        EditText edit_text_person_height = findViewById( R.id.person_height );
        String edit_text_value3 = edit_text_person_height.getText( ).toString( );
        float person_height_value = Float.parseFloat( edit_text_value3 );
        editor.putFloat( "person_height", person_height_value );

        EditText edit_text_arrow_diameter = findViewById( R.id.arrow_diameter );
        String edit_text_value4 = edit_text_arrow_diameter.getText( ).toString( );
        float arrow_diameter_value = Float.parseFloat( edit_text_value4 );
        editor.putFloat( "arrow_diameter", arrow_diameter_value );

        EditText edit_text_arrow_mass = findViewById( R.id.arrow_mass );
        String edit_text_value5 = edit_text_arrow_mass.getText( ).toString( );
        float arrow_mass_value = Float.parseFloat( edit_text_value5 );
        editor.putFloat( "arrow_mass", arrow_mass_value );

        Switch sw = findViewById( R.id.acceleration_formulas );
        editor.putBoolean( "acceleration_formulas", sw.isChecked( ) );
        Switch sw1 = findViewById( R.id.gravity_formulas );
        editor.putBoolean( "gravity_formulas", sw1.isChecked( ) );

        EditText edit_text_lens_factor = findViewById( R.id.lens_factor );
        String edit_text_value = edit_text_lens_factor.getText( ).toString( );
        int lens_factor_value = Integer.parseInt( edit_text_value );
        editor.putInt( "lens_factor", lens_factor_value );

        editor.apply( );
    }

    private void loadSettings( )
    {
        SharedPreferences settings = getSharedPreferences( "environment_settings", MODE_PRIVATE );

        int sensor_max_samples = settings.getInt( "sensor_max_samples", 5 );
        EditText edit_text_sensor_max_samples = findViewById( R.id.sensor_max_samples );
        edit_text_sensor_max_samples.setText( String.valueOf( sensor_max_samples ) );

        float arrow_velocity = settings.getFloat( "arrow_velocity", 48 );
        EditText edit_text_arrow_velocity = findViewById( R.id.arrow_velocity );
        edit_text_arrow_velocity.setText( String.valueOf( arrow_velocity ) );

        float person_height = settings.getFloat( "person_height", 1.6F );
        EditText edit_text_person_height = findViewById( R.id.person_height );
        edit_text_person_height.setText( String.valueOf( person_height ) );

        float arrow_diameter = settings.getFloat( "arrow_diameter", 0.0065F );
        EditText edit_text_arrow_diameter = findViewById( R.id.arrow_diameter );
        edit_text_arrow_diameter.setText( String.valueOf( arrow_diameter ) );

        float arrow_mass = settings.getFloat( "arrow_mass", 0.009F );
        EditText edit_text_arrow_mass = findViewById( R.id.arrow_mass );
        edit_text_arrow_mass.setText( String.valueOf( arrow_mass ) );

        boolean acceleration_formulas = settings.getBoolean( "acceleration_formulas", true );
        Switch edit_text_acceleration_formulas = findViewById( R.id.acceleration_formulas );
        edit_text_acceleration_formulas.setChecked( acceleration_formulas );

        boolean gravity_formulas = settings.getBoolean( "gravity_formulas", true );
        Switch edit_text_gravity_formulas = findViewById( R.id.gravity_formulas );
        edit_text_gravity_formulas.setChecked( gravity_formulas );

        int lens_factor = settings.getInt( "lens_factor", 100 );
        EditText edit_text_lens_factor = findViewById( R.id.lens_factor );
        edit_text_lens_factor.setText( String.valueOf( lens_factor ) );
    }
}