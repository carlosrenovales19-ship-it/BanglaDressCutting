
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.bangladresscutting.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { DressCuttingApp() }
    }
}

private val ink = Color(0xFF173B35)
private val mint = Color(0xFFEAF4EF)
private val teal = Color(0xFF267A68)
private val orange = Color(0xFFE0A14B)

@Composable
fun DressCuttingApp() {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("dress_measurements", android.content.Context.MODE_PRIVATE)
    }

    var tab by remember { mutableIntStateOf(0) }
    var garment by remember { mutableStateOf(prefs.getString("garment", "কামিজ") ?: "কামিজ") }
    var unit by remember { mutableStateOf(prefs.getString("unit", "ইঞ্চি") ?: "ইঞ্চি") }

    val fields = listOf(
        "বুকের মাপ", "কোমরের মাপ", "হিপের মাপ",
        "কাঁধের মাপ", "জামার লম্বা", "হাতার লম্বা",
        "গলার মাপ", "গলার গভীরতা", "হাতার ঘের"
    )

    val values = remember {
        fields.associateWith {
            mutableStateOf(prefs.getString(it, "") ?: "")
        }
    }

    MaterialTheme(colorScheme = lightColorScheme(primary = teal, secondary = ink)) {
        Scaffold(
            topBar = {
                Column(
                    Modifier.fillMaxWidth().background(ink).padding(18.dp)
                ) {
                    Text(
                        "বাংলা ড্রেস কাটিং",
                        color = Color.White,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "বাংলাদেশি ও আন্তর্জাতিক প্যাটার্ন",
                        color = Color(0xFFD5E9E1)
                    )
                }
            },
            bottomBar = {
                NavigationBar {
                    listOf("হোম", "মাপ দিন", "প্যাটার্ন", "তথ্য").forEachIndexed { i, name ->
                        NavigationBarItem(
                            selected = tab == i,
                            onClick = { tab = i },
                            icon = { Text(listOf("⌂", "＋", "◇", "i")[i]) },
                            label = { Text(name) }
                        )
                    }
                }
            }
        ) { padding ->
            when (tab) {
                0 -> HomeScreen(Modifier.padding(padding)) { tab = 1 }

                1 -> MeasurementScreen(
                    Modifier.padding(padding),
                    garment, { garment = it },
                    unit, { unit = it },
                    fields, values,
                    onSave = {
                        prefs.edit()
                            .putString("garment", garment)
                            .putString("unit", unit)
                            .also { editor ->
                                values.forEach { (key, state) ->
                                    editor.putString(key, state.value)
                                }
                            }.apply()

                        Toast.makeText(context, "মাপ সংরক্ষণ হয়েছে", Toast.LENGTH_SHORT).show()
                    },
                    onNext = { tab = 2 }
                )

                2 -> PatternScreen(
                    Modifier.padding(padding),
                    garment,
                    values.mapValues { it.value.value },
                    unit
                )

                else -> AboutScreen(Modifier.padding(padding))
            }
        }
    }
}

@Composable
fun HomeScreen(modifier: Modifier, onStart: () -> Unit) {
    LazyColumn(
        modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = mint),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        "আপনার মাপ, আপনার প্যাটার্ন",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = ink
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("পোশাক নির্বাচন করুন, মাপ লিখুন এবং কাটিংয়ের প্রাথমিক হিসাব দেখুন।")
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) {
                        Text("নতুন প্যাটার্ন তৈরি করুন")
                    }
                }
            }
        }

        item {
            Text("পোশাকের তালিকা", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = ink)
        }

        items(listOf("কামিজ", "ব্লাউজ", "সালোয়ার", "পায়জামা", "প্যান্ট", "স্কার্ট", "গাউন", "শার্ট")) {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("✂", color = teal, fontSize = 22.sp)
                    Spacer(Modifier.width(12.dp))
                    Text(it, fontWeight = FontWeight.SemiBold, color = ink)
                }
            }
        }
    }
}

@Composable
fun MeasurementScreen(
    modifier: Modifier,
    garment: String,
    onGarment: (String) -> Unit,
    unit: String,
    onUnit: (String) -> Unit,
    fields: List<String>,
    values: Map<String, MutableState<String>>,
    onSave: () -> Unit,
    onNext: () -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("১. পোশাক নির্বাচন করুন", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = ink)
        }

        item {
            var expanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = garment,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("পোশাকের ধরন") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    listOf("কামিজ", "ব্লাউজ", "সালোয়ার", "পায়জামা", "প্যান্ট", "স্কার্ট", "গাউন", "শার্ট").forEach {
                        DropdownMenuItem(
                            text = { Text(it) },
                            onClick = {
                                onGarment(it)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        item { Text("২. মাপের একক", fontWeight = FontWeight.Bold, color = ink) }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("ইঞ্চি", "সেন্টিমিটার").forEach {
                    FilterChip(
                        selected = unit == it,
                        onClick = { onUnit(it) },
                        label = { Text(it) }
                    )
                }
            }
        }

        item {
            Text("৩. শরীরের মাপ লিখুন", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = ink)
        }

        items(fields) { field ->
            OutlinedTextField(
                value = values.getValue(field).value,
                onValueChange = { v ->
                    values.getValue(field).value = v.filter { it.isDigit() || it == '.' }
                },
                label = { Text(field) },
                placeholder = { Text("মাপ লিখুন ($unit)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(onClick = onSave, modifier = Modifier.weight(1f)) {
                    Text("মাপ সংরক্ষণ")
                }

                Button(
                    onClick = {
                        val hasValue = values.values.any {
                            it.value.toDoubleOrNull()?.let { n -> n > 0 } == true
                        }

                        if (hasValue) onNext()
                        else Toast.makeText(context, "আগে অন্তত একটি সঠিক মাপ লিখুন", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("প্যাটার্ন দেখুন")
                }
            }
        }
    }
}

@Composable
fun PatternScreen(
    modifier: Modifier,
    garment: String,
    measurements: Map<String, String>,
    unit: String
) {
    var method by remember { mutableStateOf("বাংলাদেশি") }
    var view by remember { mutableStateOf("একসঙ্গে") }

    val chest = measurements["বুকের মাপ"]?.toDoubleOrNull()
    val waist = measurements["কোমরের মাপ"]?.toDoubleOrNull()
    val hip = measurements["হিপের মাপ"]?.toDoubleOrNull()
    val length = measurements["জামার লম্বা"]?.toDoubleOrNull()
    val shoulder = measurements["কাঁধের মাপ"]?.toDoubleOrNull()
    val neck = measurements["গলার মাপ"]?.toDoubleOrNull()
    val neckDepth = measurements["গলার গভীরতা"]?.toDoubleOrNull()

    val ease = if (method == "বাংলাদেশি") 2.0 else 1.5
    val seam = if (method == "বাংলাদেশি") 0.5 else 0.625

    val quarterChest = chest?.let { it / 4.0 + ease }
    val quarterWaist = waist?.let { it / 4.0 + ease }
    val quarterHip = hip?.let { it / 4.0 + ease }

    LazyColumn(
        modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                "$garment — কাটিং প্যাটার্ন",
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = ink
            )
        }

        item {
            Text("১. কাটিং পদ্ধতি নির্বাচন করুন", fontWeight = FontWeight.Bold, color = ink)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("বাংলাদেশি", "আন্তর্জাতিক").forEach {
                    FilterChip(
                        selected = method == it,
                        onClick = { method = it },
                        label = { Text(it) }
                    )
                }
            }
        }

        item {
            Text("২. প্যাটার্ন দেখার ধরন", fontWeight = FontWeight.Bold, color = ink)
            Column {
                listOf("সামনে", "পেছনে", "একসঙ্গে").forEach {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = view == it,
                            onClick = { view = it }
                        )
                        Text(it)
                    }
                }
            }
        }

        item {
            Text(
                "$method পদ্ধতি — ${if (view == "একসঙ্গে") "সামনে ও পেছনে" else "$view প্যাটার্ন"}",
                fontWeight = FontWeight.Bold,
                color = teal
            )
        }

        item {
            if (view == "একসঙ্গে") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PatternDrawing(
                        "সামনে",
                        neckDepth ?: 3.0,
                        Modifier.weight(1f)
                    )
                    PatternDrawing(
                        "পেছনে",
                        1.5,
                        Modifier.weight(1f)
                    )
                }
            } else {
                PatternDrawing(
                    view,
                    if (view == "সামনে") neckDepth ?: 3.0 else 1.5,
                    Modifier.fillMaxWidth()
                )
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = mint)) {
                Column(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("৩. কাটিংয়ের প্রাথমিক হিসাব", fontWeight = FontWeight.Bold, color = ink)

                    Text("বুকের ১/৪ + ঢিল = ${quarterChest?.let { "%.2f".format(it) } ?: "মাপ দিন"} $unit")
                    Text("কোমরের ১/৪ + ঢিল = ${quarterWaist?.let { "%.2f".format(it) } ?: "মাপ দিন"} $unit")
                    Text("হিপের ১/৪ + ঢিল = ${quarterHip?.let { "%.2f".format(it) } ?: "মাপ দিন"} $unit")
                    Text("কাঁধের মাপ = ${shoulder ?: "মাপ দিন"} $unit")
                    Text("গলার মাপ = ${neck ?: "মাপ দিন"} $unit")
                    Text("গলার গভীরতা = ${neckDepth ?: "মাপ দিন"} $unit")
                    Text("জামার লম্বা = ${length ?: "মাপ দিন"} $unit")

                    Divider()

                    Text("নির্বাচিত পদ্ধতি: $method")
                    Text("হিসাবের জন্য ব্যবহৃত ঢিল: $ease $unit")
                    Text("প্রস্তাবিত সেলাই ভাতা: $seam $unit")
                }
            }
        }

        item {
            Text(
                "সতর্কতা: ঢিল ও সেলাই ভাতার মান এখানে প্রাথমিক নমুনা হিসেবে রাখা হয়েছে; এগুলো সব কাপড় ও সব ডিজাইনের জন্য নির্ভুল নয়। আন্তর্জাতিক পদ্ধতি কোনো একক বিশ্বজনীন সূত্র নয়। বাস্তবে কাপড় কাটার আগে অভিজ্ঞ প্যাটার্ন মেকারের সাহায্যে যাচাই করুন।",
                color = Color(0xFF8A4B16),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun PatternDrawing(
    side: String,
    neckDepth: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("$side প্যাটার্ন", fontWeight = FontWeight.Bold, color = ink)

            Canvas(
                Modifier.fillMaxWidth().height(220.dp).background(Color(0xFFFCFDFC))
            ) {
                val w = size.width
                val h = size.height

                val path = Path().apply {
                    moveTo(w * 0.20f, h * 0.10f)
                    lineTo(w * 0.80f, h * 0.10f)
                    lineTo(w * 0.70f, h * 0.30f)
                    lineTo(w * 0.75f, h * 0.90f)
                    lineTo(w * 0.25f, h * 0.90f)
                    lineTo(w * 0.30f, h * 0.30f)
                    close()
                }

                drawPath(path, color = teal, style = Stroke(width = 4f))

                val depth = if (side == "সামনে") 0.23f else 0.10f

                drawLine(
                    orange,
                    Offset(w * 0.20f, h * 0.10f),
                    Offset(w * 0.50f, h * depth),
                    strokeWidth = 3f
                )

                drawLine(
                    orange,
                    Offset(w * 0.50f, h * depth),
                    Offset(w * 0.80f, h * 0.10f),
                    strokeWidth = 3f
                )

                drawLine(
                    Color.LightGray,
                    Offset(w * 0.50f, h * 0.10f),
                    Offset(w * 0.50f, h * 0.90f),
                    strokeWidth = 1.5f
                )

                drawLine(
                    Color.LightGray,
                    Offset(w * 0.15f, h * 0.35f),
                    Offset(w * 0.85f, h * 0.35f),
                    strokeWidth = 1.5f
                )
            }

            Text(
                if (side == "সামনে") "সামনের গলার গভীরতা" else "পেছনের গলার রেখা",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun AboutScreen(modifier: Modifier) {
    Column(
        modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("অ্যাপ সম্পর্কে", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ink)
        Text("বাংলা ড্রেস কাটিং — পোশাকের মাপ ও প্যাটার্ন শেখার অ্যাপ।")
        Text("বর্তমান সুবিধা:")
        listOf(
            "বাংলাদেশি ও আন্তর্জাতিক পদ্ধতি নির্বাচন",
            "সামনে ও পেছনের প্যাটার্ন আলাদা দেখা",
            "সামনে ও পেছনের প্যাটার্ন একসঙ্গে দেখা",
            "প্রাথমিক মাপ ও কাটিং হিসাব",
            "মাপ ফোনে সংরক্ষণ"
        ).forEach {
            Text("• $it")
        }
        Text("সংস্করণ ১.২", color = Color.Gray, fontSize = 12.sp)
    }
}
