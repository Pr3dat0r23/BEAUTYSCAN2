package com.example.beautyscan2

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScannerFragment : Fragment() {

    private lateinit var viewFinder: PreviewView
    private lateinit var cameraExecutor: ExecutorService
    private var isScanning = true // Blokada, żeby nie zeskanować kodu 10 razy w ułamku sekundy

    // Automatyczne pytanie o zgodę na aparat
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(requireContext(), "Musisz zezwolić na użycie aparatu!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scanner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewFinder = view.findViewById(R.id.viewFinder)
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Sprawdzamy czy mamy już zgodę, jeśli nie - pytamy użytkownika
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Ustawienie podglądu z kamery na ekranie
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(viewFinder.surfaceProvider)
            }

            // Ustawienie "analizatora" klatek przez sztuczną inteligencję
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImageProxy(imageProxy)
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, preview, imageAnalyzer)
            } catch (exc: Exception) {
                Toast.makeText(requireContext(), "Błąd uruchamiania aparatu.", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        if (!isScanning) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            // Konfiguracja ML Kit - mówimy mu, żeby szukał standardowych kodów kreskowych
            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8)
                .build()
            val scanner = BarcodeScanning.getClient(options)

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        val rawValue = barcode.rawValue
                        if (rawValue != null) {
                            isScanning = false // Sukces! Blokujemy dalsze skanowanie

                            requireActivity().runOnUiThread {
                                // 1. Przekazujemy zeskanowany kod do naszego "Mózgu"
                                val viewModel = ViewModelProvider(requireActivity())[ScannerViewModel::class.java]
                                viewModel.searchBarcode(rawValue)

                                // 2. Zamykamy skaner i wracamy na poprzedni ekran zobaczyć wyniki
                                findNavController().popBackStack()
                            }
                            break
                        }
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close() // Obowiązkowo zwalniamy klatkę, inaczej obraz zamarznie
                }
        } else {
            imageProxy.close()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}