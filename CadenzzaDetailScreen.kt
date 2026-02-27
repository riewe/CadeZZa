package com.riewe.cadezza.screens.detail

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.riewe.cadezza.ui.CadeZZaTheme
import com.riewe.cadezza.R

import Icons.Default.Stop  // Replaced Close with Stop

class CadenzzaDetailScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) 
        setContent { 
            CadenzzaDetailContent() 
        }
    }
}

@Composable
fun CadenzzaDetailContent() {
    // Other UI elements
    Button(onClick = { /* Do something */ }, colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)) {
        Icon(Icons.Default.Stop, contentDescription = "Close") // Replaced Close with Stop
        Text(text = "Close")
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CadenzzaDetailContent()
}