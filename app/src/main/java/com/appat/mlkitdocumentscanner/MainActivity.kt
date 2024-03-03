package com.appat.mlkitdocumentscanner

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import com.appat.mlkitdocumentscanner.ui.theme.MLKitDocumentScannerTheme
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MLKitDocumentScannerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MLKitDocumentScannerContent()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MLKitDocumentScannerContent() {
    val activity = LocalContext.current as MainActivity
    val options = remember {
        GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(false)
            .setResultFormats(RESULT_FORMAT_PDF)
            .setScannerMode(SCANNER_MODE_FULL)
            .build()
    }
    val docs = remember {
        mutableStateListOf<GmsDocumentScanningResult.Pdf>()
    }
    val scannerLauncher = rememberLauncherForActivityResult(contract =
    ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val scanningResult =
                GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            scanningResult?.pdf?.let { pdf ->
                docs.add(pdf)
            }
        }
    }
    val scanner = remember {
        GmsDocumentScanning.getClient(options)
    }
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("MLKit Doc Scanner") },
                colors = TopAppBarDefaults.mediumTopAppBarColors()
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { scanner.getStartScanIntent(activity)
                    .addOnSuccessListener { intentSender ->
                        scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                    }
                    .addOnFailureListener {

                    } },
                text = {
                    Text(text = "Scan")
                },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.capture),
                        contentDescription = null,
                    )
                }
            )
        },
        content = {
            Surface(modifier = Modifier.padding(it)) {
                LazyColumn(modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(10.dp)
                ) {
                    items(items = docs) {
                        Card(modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .padding(10.dp),
                            onClick = {
                            val pdfUri = it.uri
                            val pdfFileUri = FileProvider.getUriForFile(
                                activity,
                                activity.packageName + ".provider",
                                pdfUri.toFile()
                            )
                            val browserIntent = Intent(Intent.ACTION_VIEW, pdfFileUri)
                            browserIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                            activity.startActivity(browserIntent)
                        }) {
                            Row(modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    modifier = Modifier.fillMaxHeight(),
                                    painter = painterResource(id = R.drawable.pdf_icon),
                                    contentDescription = null,
                                )
                                Text(text = it.uri.lastPathSegment ?: "")
                            }
                        }
                    }
                }
            }
        }
    )
}