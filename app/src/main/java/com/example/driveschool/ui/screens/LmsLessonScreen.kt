package com.example.driveschool.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.driveschool.data.model.LessonEntity
import com.example.driveschool.data.model.LessonType
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import org.json.JSONArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LmsLessonScreen(
  lesson: LessonEntity,
  currentLocale: String,
  onBack: () -> Unit,
  onCompleteLesson: (Double?) -> Unit
) {
  var isPlayingVideo by remember { mutableStateOf(false) }
  var videoProgress by remember { mutableFloatStateOf(0.35f) }

  // Quiz state
  var currentQuestionIndex by remember { mutableIntStateOf(0) }
  var selectedAnswerIndex by remember { mutableStateOf<Int?>(null) }
  var isAnswerSubmitted by remember { mutableStateOf(false) }
  var correctAnswersCount by remember { mutableIntStateOf(0) }
  var quizFinished by remember { mutableStateOf(false) }

  val questions = remember(lesson.quizQuestionsJson) {
    val list = mutableListOf<QuizQuestion>()
    try {
      if (lesson.quizQuestionsJson.isNotBlank()) {
        val array = JSONArray(lesson.quizQuestionsJson)
        for (i in 0 until array.length()) {
          val obj = array.getJSONObject(i)
          val q = obj.getString("q")
          val opts = mutableListOf<String>()
          val optsArr = obj.getJSONArray("options")
          for (j in 0 until optsArr.length()) {
            opts.add(optsArr.getString(j))
          }
          val correct = obj.getInt("correct")
          val exp = obj.optString("exp", "")
          list.add(QuizQuestion(q, opts, correct, exp))
        }
      }
    } catch (_: Exception) {}
    list
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = if (currentLocale == "fr") lesson.titleFr else lesson.title,
            maxLines = 1,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("lesson_back_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          if (lesson.isOnboarding) {
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = EmeraldSuccess.copy(alpha = 0.2f),
              modifier = Modifier.padding(end = 12.dp)
            ) {
              Text(
                text = "ONBOARDING",
                color = EmeraldSuccess,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = NavyPrimary,
          titleContentColor = Color.White,
          navigationIconContentColor = Color.White
        )
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Module Title
      Text(
        text = if (currentLocale == "fr") lesson.moduleTitleFr else lesson.moduleTitle,
        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
      )

      // VIDEO PLAYER VIEW (FR-13: Streamed from external CDN)
      if (lesson.type == LessonType.VIDEO) {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = NavyDark),
          modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .testTag("video_player_card")
        ) {
          Box(modifier = Modifier.fillMaxSize()) {
            // External CDN badge (FR-13)
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = Color.Black.copy(alpha = 0.6f),
              modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.Cloud, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "CDN STREAM • Adaptive Bitrate",
                  color = Color.White,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }

            // Central Play / Pause Button
            IconButton(
              onClick = { isPlayingVideo = !isPlayingVideo },
              modifier = Modifier
                .size(64.dp)
                .align(Alignment.Center)
                .background(AmberAccent, CircleShape)
                .testTag("play_pause_video_button")
            ) {
              Icon(
                imageVector = if (isPlayingVideo) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlayingVideo) "Pause" else "Play",
                tint = NavyDark,
                modifier = Modifier.size(36.dp)
              )
            }

            // Scrubber Bar
            Column(
              modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp)
            ) {
              LinearProgressIndicator(
                progress = { videoProgress },
                modifier = Modifier.fillMaxWidth(),
                color = AmberAccent,
                trackColor = Color.DarkGray
              )
              Spacer(modifier = Modifier.height(4.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = if (isPlayingVideo) "05:14 / 15:00" else "00:00 / 15:00",
                  color = Color.LightGray,
                  fontSize = 11.sp
                )
                Text(
                  text = "1080p HD",
                  color = AmberAccent,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
      }

      // DOCUMENT CONTENT
      val bodyText = if (currentLocale == "fr") lesson.contentBodyFr else lesson.contentBodyEn
      if (bodyText.isNotBlank()) {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color(0xFF0284C7))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (currentLocale == "fr") "Manuel Officiel du Code de la Route" else "Official Highway Code Study Material",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = bodyText,
              style = MaterialTheme.typography.bodyMedium.copy(
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
            )
          }
        }
      }

      // INTERACTIVE QUIZ ENGINE (FR-14 & FR-15)
      if (lesson.type == LessonType.QUIZ && questions.isNotEmpty()) {
        if (!quizFinished) {
          val q = questions[currentQuestionIndex]

          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier.fillMaxWidth().testTag("quiz_card")
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Question ${currentQuestionIndex + 1} / ${questions.size}",
                  style = MaterialTheme.typography.bodySmall.copy(color = AmberAccent, fontWeight = FontWeight.Bold)
                )
                Text(
                  text = if (currentLocale == "fr") "Seuil Réussite : 80%" else "Pass Mark: 80%",
                  style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 11.sp)
                )
              }

              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = q.question,
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp
                )
              )

              Spacer(modifier = Modifier.height(14.dp))

              // Options
              q.options.forEachIndexed { index, optionText ->
                val isSelected = selectedAnswerIndex == index
                val isCorrect = index == q.correctIndex
                val backgroundColor = when {
                  !isAnswerSubmitted && isSelected -> AmberAccent.copy(alpha = 0.2f)
                  isAnswerSubmitted && isCorrect -> EmeraldSuccess.copy(alpha = 0.25f)
                  isAnswerSubmitted && isSelected && !isCorrect -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                  else -> MaterialTheme.colorScheme.surfaceVariant
                }

                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = backgroundColor,
                  onClick = {
                    if (!isAnswerSubmitted) {
                      selectedAnswerIndex = index
                    }
                  },
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("quiz_option_$index")
                ) {
                  Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    RadioButton(
                      selected = isSelected,
                      onClick = { if (!isAnswerSubmitted) selectedAnswerIndex = index },
                      enabled = !isAnswerSubmitted
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = optionText,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                      modifier = Modifier.weight(1f)
                    )
                    if (isAnswerSubmitted && isCorrect) {
                      Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess)
                    } else if (isAnswerSubmitted && isSelected && !isCorrect) {
                      Icon(Icons.Default.Cancel, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    }
                  }
                }
              }

              // Explanation after submitting
              if (isAnswerSubmitted && q.explanation.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = Color(0xFFF8FAFC),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                      text = if (currentLocale == "fr") "💡 Explication Code de la Route :" else "💡 Highway Code Rule Explanation:",
                      fontWeight = FontWeight.Bold,
                      fontSize = 12.sp,
                      color = NavyPrimary
                    )
                    Text(
                      text = q.explanation,
                      style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF334155))
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(16.dp))

              // Action button
              if (!isAnswerSubmitted) {
                Button(
                  onClick = {
                    if (selectedAnswerIndex != null) {
                      isAnswerSubmitted = true
                      if (selectedAnswerIndex == q.correctIndex) {
                        correctAnswersCount++
                      }
                    }
                  },
                  enabled = selectedAnswerIndex != null,
                  colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.fillMaxWidth().testTag("submit_quiz_answer_button")
                ) {
                  Text(if (currentLocale == "fr") "Valider Réponse" else "Submit Answer")
                }
              } else {
                Button(
                  onClick = {
                    if (currentQuestionIndex + 1 < questions.size) {
                      currentQuestionIndex++
                      selectedAnswerIndex = null
                      isAnswerSubmitted = false
                    } else {
                      quizFinished = true
                    }
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.fillMaxWidth().testTag("next_quiz_question_button")
                ) {
                  Text(
                    if (currentQuestionIndex + 1 < questions.size)
                      (if (currentLocale == "fr") "Question Suivante →" else "Next Question →")
                    else
                      (if (currentLocale == "fr") "Voir Résultats Finaux" else "View Final Results")
                  )
                }
              }
            }
          }
        } else {
          // QUIZ RESULTS
          val scorePercent = (correctAnswersCount.toDouble() / questions.size) * 100
          val passed = scorePercent >= 80.0

          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (passed) Color(0xFF0F2744) else Color(0xFF450A0A)),
            modifier = Modifier.fillMaxWidth().testTag("quiz_results_card")
          ) {
            Column(
              modifier = Modifier.padding(20.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(
                imageVector = if (passed) Icons.Default.EmojiEvents else Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = if (passed) AmberAccent else Color(0xFFF87171),
                modifier = Modifier.size(54.dp)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = if (passed)
                  (if (currentLocale == "fr") "Félicitations ! Évaluation Réussie" else "Congratulations! Assessment Passed")
                else
                  (if (currentLocale == "fr") "Seuil non atteint" else "Needs Improvement"),
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 18.sp
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "$correctAnswersCount / ${questions.size} correct (${scorePercent.toInt()}%)",
                color = if (passed) EmeraldSuccess else Color(0xFFFCA5A5),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
              )

              Spacer(modifier = Modifier.height(16.dp))
              Button(
                onClick = { onCompleteLesson(scorePercent) },
                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().testTag("finish_lesson_button")
              ) {
                Text(
                  text = if (currentLocale == "fr") "Enregistrer et Terminer Leçon" else "Save Progress & Finish Lesson",
                  color = NavyDark,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
      }

      // MARK AS COMPLETED BUTTON (For Document or Video Lessons)
      if (lesson.type != LessonType.QUIZ) {
        Button(
          onClick = { onCompleteLesson(100.0) },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("mark_lesson_done_button")
        ) {
          Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (currentLocale == "fr") "Marquer cette Leçon comme Terminée" else "Mark Lesson as Completed",
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}

data class QuizQuestion(
  val question: String,
  val options: List<String>,
  val correctIndex: Int,
  val explanation: String
)
