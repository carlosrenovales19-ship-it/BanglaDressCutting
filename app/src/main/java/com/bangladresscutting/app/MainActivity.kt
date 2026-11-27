package com.bangladresscutting.app

import android.os.Bundle
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { DressCuttingApp() }
    }
}

private val ink = Color(0xFF173B35)
private val mint = Color(0xFFEAF4EF)
private val teal = Color(0xFF267A68)

@Composable
fun DressCuttingApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("dress_measurements", android.content.Context.MODE_PRIVATE) }
    var tab by remember { mutableStateOf(0) }
    var garment by remember { mutableStateOf(prefs.getString("garment", "কামিজ") ?: "কামিজ") }
    var unit by remember { mutableStateOf(prefs.getString("unit", "ইঞ্চি") ?: "ইঞ্চি") }
    val fields = listOf("বুকের মাপ", "কোমরের মাপ", "হিপের মাপ", "কাঁধের মাপ", "জামার লম্বা", "হাতার লম্বা", "গলার মাপ")
    val values = remember { fields.associateWith { mutableStateOf(prefs.getString(it, "") ?: "") } }
    MaterialTheme(colorScheme = lightColorScheme(primary = teal, secondary = ink, background = Color(0xFFF7FAF8))) {
        Scaffold(
            topBar = { Column(Modifier.fillMaxWidth().background(ink).padding(horizontal = 20.dp, vertical = 18.dp)) {
                Text("বাংলা ড্রেস কাটিং", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                Text("মাপ থেকে প্যাটার্ন — সহজ বাংলায়", color = Color(0xFFD5E9E1), fontSize = 13.sp)
            } },
            bottomBar = { NavigationBar {
                listOf("হোম", "মাপ দিন", "প্যাটার্ন", "তথ্য").forEachIndexed { i, s ->
                    NavigationBarItem(selected = tab == i, onClick = { tab = i }, icon = { Text(listOf("⌂", "＋", "◇", "i")[i], fontSize = 18.sp) }, label = { Text(s) })
                }
            } }
        ) { padding ->
            when (tab) {
                0 -> HomeScreen(Modifier.padding(padding), onStart = { tab = 1 })
                1 -> MeasurementScreen(Modifier.padding(padding), garment, { garment = it }, unit, { unit = it }, fields, values, onSave = {
                    prefs.edit().putString("garment", garment).putString("unit", unit).also { editor -> values.forEach { (key, state) -> editor.putString(key, state.value) } }.apply()
                    Toast.makeText(context, "আপনার মাপ এই ফোনে সংরক্ষণ হয়েছে", Toast.LENGTH_SHORT).show()
                }, onNext = { tab = 2 })
                2 -> PatternScreen(Modifier.padding(padding), garment, values.mapValues { it.value.value }, unit)
                else -> AboutScreen(Modifier.padding(padding))
            }
        }
    }
}

@Composable
fun HomeScreen(modifier: Modifier, onStart: () -> Unit) {
    LazyColumn(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = mint), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Text("আপনার মাপ, আপনার প্যাটার্ন", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ink)
                    Spacer(Modifier.height(8.dp))
                    Text("পোশাক নির্বাচন করুন, শরীরের মাপ লিখুন এবং প্রাথমিক কাটিং হিসাব দেখুন।", color = ink)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) { Text("নতুন প্যাটার্ন তৈরি করুন") }
                }
            }
        }
        item { Text("যে পোশাকের প্যাটার্ন তৈরি করতে পারবেন", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = ink) }
        items(listOf("কামিজ", "ব্লাউজ", "সালোয়ার", "পায়জামা", "প্যান্ট", "স্কার্ট", "গাউন", "শার্ট", "হাতা ও গলা")) { name ->
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("✂", color = teal, fontSize = 22.sp)
                    Spacer(Modifier.width(12.dp)); Text(name, fontWeight = FontWeight.SemiBold, color = ink)
                    Spacer(Modifier.weight(1f)); Text("›", color = teal, fontSize = 24.sp)
                }
            }
        }
    }
}

@Composable
fun MeasurementScreen(modifier: Modifier, garment: String, onGarment: (String) -> Unit, unit: String, onUnit: (String) -> Unit, fields: List<String>, values: Map<String, MutableState<String>>, onSave: () -> Unit, onNext: () -> Unit) {
    val context = LocalContext.current
    LazyColumn(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("১. পোশাক নির্বাচন করুন", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = ink) }
        item {
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(value = garment, onValueChange = {}, readOnly = true, label = { Text("পোশাকের ধরন") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    listOf("কামিজ", "ব্লাউজ", "সালোয়ার", "পায়জামা", "প্যান্ট", "স্কার্ট", "গাউন", "শার্ট").forEach { DropdownMenuItem(text = { Text(it) }, onClick = { onGarment(it); expanded = false }) }
                }
            }
        }
        item { Text("২. মাপের একক", fontWeight = FontWeight.Bold, color = ink) }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("ইঞ্চি", "সেন্টিমিটার").forEach { FilterChip(selected = unit == it, onClick = { onUnit(it) }, label = { Text(it) }) } } }
        item { Text("৩. শরীরের মাপ লিখুন", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = ink) }
        items(fields) { field ->
            OutlinedTextField(value = values.getValue(field).value, onValueChange = { v -> values.getValue(field).value = v.filter { it.isDigit() || it == '.' } }, label = { Text(field) }, placeholder = { Text("মাপ লিখুন ($unit)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onSave, modifier = Modifier.weight(1f)) { Text("মাপ সংরক্ষণ") }
                Button(onClick = {
                    val hasValue = values.values.any { it.value.toDoubleOrNull()?.let { n -> n > 0 } == true }
                    if (hasValue) onNext() else Toast.makeText(context, "আগে অন্তত একটি সঠিক মাপ লিখুন", Toast.LENGTH_SHORT).show()
                }, modifier = Modifier.weight(1f)) { Text("প্যাটার্ন দেখুন") }
            }
        }
        item { Text("আপনার মাপ এই ফোনে সংরক্ষণ করা যাবে। পরেরবার অ্যাপ খুললে শেষ সংরক্ষিত মাপ পাওয়া যাবে।", color = teal, fontSize = 12.sp) }
        item { Text("নোট: সঠিক ফিটিংয়ের জন্য মাপ নেওয়ার সময় ফিতা শরীরের সঙ্গে আরামদায়কভাবে রাখুন। কাটার আগে দর্জির মাধ্যমে হিসাব যাচাই করুন।", color = Color.Gray, fontSize = 12.sp) }
    }
}

@Composable
fun PatternScreen(modifier: Modifier, garment: String, measurements: Map<String, String>, unit: String) {
    val chest = measurements["বুকের মাপ"]?.toDoubleOrNull()
    val waist = measurements["কোমরের মাপ"]?.toDoubleOrNull()
    val length = measurements["জামার লম্বা"]?.toDoubleOrNull()
    LazyColumn(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text("$garment — প্যাটার্ন প্রিভিউ", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = ink) }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.fillMaxWidth().padding(12.dp)) {
                    Canvas(Modifier.fillMaxWidth().height(300.dp).background(Color(0xFFFCFDFC))) {
                        val p = Path().apply { moveTo(size.width*.25f, size.height*.12f); lineTo(size.width*.75f, size.height*.12f); lineTo(size.width*.68f, size.height*.36f); lineTo(size.width*.72f, size.height*.88f); lineTo(size.width*.28f, size.height*.88f); lineTo(size.width*.32f, size.height*.36f); close() }
                        drawPath(p, color = teal, style = Stroke(width = 4f))
                        drawLine(Color(0xFFE0A14B), Offset(size.width*.25f,size.height*.12f), Offset(size.width*.75f,size.height*.12f), strokeWidth = 2f)
                        drawLine(Color.LightGray, Offset(size.width*.5f,size.height*.12f), Offset(size.width*.5f,size.height*.88f), strokeWidth = 1.5f)
                        drawLine(Color.LightGray, Offset(size.width*.18f,size.height*.36f), Offset(size.width*.82f,size.height*.36f), strokeWidth = 1.5f)
                    }
                    Text("প্রাথমিক প্যাটার্নের রেখাচিত্র", color = ink, fontWeight = FontWeight.SemiBold)
                    Text("এটি একটি ধারণামূলক প্রিভিউ; এটি এখনো স্কেল-নির্ভুল কাটিং পেপার নয়।", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
        item { Text("আপনার দেওয়া মাপ", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = ink) }
        items(measurements.filterValues { it.isNotBlank() }.toList()) { (k,v) ->
            Row(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(10.dp)).padding(13.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(k); Text("$v $unit", fontWeight = FontWeight.Bold, color = teal) }
        }
        item {
            val quarterChest = chest?.div(4.0)
            val quarterWaist = waist?.div(4.0)
            Card(colors = CardDefaults.cardColors(containerColor = mint)) {
                Column(Modifier.padding(16.dp)) {
                    Text("প্রাথমিক হিসাব", fontWeight = FontWeight.Bold, color = ink)
                    Text("বুকের ১/৪ = ${quarterChest?.let { "%.2f".format(it) } ?: "মাপ দিন"} $unit")
                    Text("কোমরের ১/৪ = ${quarterWaist?.let { "%.2f".format(it) } ?: "মাপ দিন"} $unit")
                    Text("জামার লম্বা = ${length?.let { "%.2f".format(it) } ?: "মাপ দিন"} $unit")
                }
            }
        }
        item { Text("গুরুত্বপূর্ণ: পোশাকভেদে ঢিল, সেলাই ভাতা, ডার্ট ও কাটিং নিয়ম আলাদা। অভিজ্ঞ দর্জির যাচাই ছাড়া এই প্রাথমিক হিসাব সরাসরি কাপড়ে কাটবেন না।", color = Color(0xFF8A4B16), fontSize = 12.sp) }
    }
}

@Composable
fun AboutScreen(modifier: Modifier) {
    Column(modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("অ্যাপ সম্পর্কে", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ink)
        Text("বাংলা ড্রেস কাটিং একটি বাংলা ভাষার পোশাকের মাপ ও প্যাটার্ন শেখার অ্যাপের প্রাথমিক সংস্করণ।")
        Text("পরবর্তী সংস্করণে যুক্ত করার পরিকল্পনা:")
        listOf("প্রতিটি পোশাকের আলাদা বৈজ্ঞানিক কাটিং সূত্র", "হাতা, গলা ও কলারের ডিজাইন লাইব্রেরি", "মাপ সংরক্ষণ ও গ্রাহক তালিকা", "স্কেল-নির্ভুল PDF প্যাটার্ন ও প্রিন্ট", "অফলাইন ব্যবহার ও বাংলা নির্দেশিকা").forEach { Text("• $it") }
        Text("সংস্করণ ১.১ — মাপ সংরক্ষণসহ প্রোটোটাইপ", color = Color.Gray, fontSize = 12.sp)
    }
}
