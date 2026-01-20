package com.example.moodmate.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.moodmate.R
import com.example.moodmate.components.EditDetailsButton
import com.example.moodmate.components.EditScaffold

@Composable
fun MoodDetailsScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    EditScaffold(navController = navController) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MoodEmojiCard(
                emoji = "😊",
                dateTime = "20.01.2026 14:30",
                modifier = modifier
            )

            DetailedNoteCard(
                note = "Bugün genel olarak oldukça verimli bir gündü. Sabah erken kalkıp rutinlerimi tamamlamak beni güne çok iyi hazırladı. " +
                        "İş yerindeki projelerde beklediğimden daha hızlı ilerleme kaydettim. Öğle yemeğinde biraz dinlenme fırsatı buldum. " +
                        "Akşamüzeri arkadaşlarımla kahve içmek günün tüm yorgunluğunu üzerimden attı. " +
                        "Kendimi zihinsel olarak çok berrak ve huzurlu hissediyorum. " +
                        "Yarınki planlarımı şimdiden hazırladım ve motivasyonum oldukça yüksek. " +
                        "Küçük şeylerden mutlu olmayı başardığım bir gün daha geride kalıyor.",
                modifier = modifier
            )

            EditDetailsButton(
                text = stringResource(id = R.string.edit),
                icon = Icons.Default.Edit,
                onClick = { },
                containerColor = colorResource(id = R.color.acik_mavi)
            )

            EditDetailsButton(
                text = stringResource(id = R.string.delete),
                icon = Icons.Default.Delete,
                onClick = { },
                containerColor = Color.Red
            )

            EditDetailsButton(
                text = stringResource(id = R.string.ask_ai),
                icon = Icons.Default.AutoAwesome,
                onClick = { },
                containerColor = colorResource(id = R.color.ai_purple)
            )
        }
    }
}

@Composable
fun MoodEmojiCard(
    emoji: String,
    dateTime: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.displayLarge,
                modifier = modifier.padding(bottom = 24.dp)
            )

            Surface(
                color = colorResource(id = R.color.light_gray_background),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        tint = colorResource(id = R.color.date_time_color)
                    )

                    Text(
                        text = dateTime,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = colorResource(id = R.color.date_time_color),
                        modifier = modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DetailedNoteCard(
    note: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier.padding(bottom = 12.dp)
            ) {
                Surface(
                    color = colorResource(id = R.color.success_green).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = "📝",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = modifier.padding(8.dp)
                    )
                }
                Text(
                    text = stringResource(id = R.string.detailed_note),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = note,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MoodDetailsScreenPreview() {
    MoodDetailsScreen(navController = rememberNavController())
}