package com.vibecode.minigo.ui.tutorial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vibecode.minigo.R
import com.vibecode.minigo.engine.Board
import com.vibecode.minigo.engine.Point
import com.vibecode.minigo.ui.common.BackTopBar
import com.vibecode.minigo.ui.game.GoBoard

private data class TutorialPage(
    val titleRes: Int,
    val bodyRes: Int,
    val rows: List<String>,
    val marks: Set<Point> = emptySet(),
    val showCoordinates: Boolean = false,
)

private val TutorialPages = listOf(
    TutorialPage(
        titleRes = R.string.tut_board_title,
        bodyRes = R.string.tut_board_body,
        rows = listOf(
            ".........",
            ".........",
            ".........",
            ".........",
            ".........",
            ".........",
            ".........",
            ".........",
            ".........",
        ),
        showCoordinates = true,
    ),
    TutorialPage(
        titleRes = R.string.tut_liberty_title,
        bodyRes = R.string.tut_liberty_body,
        rows = listOf(
            ".......",
            ".......",
            ".......",
            "...X...",
            ".......",
            ".......",
            ".......",
        ),
        marks = setOf(Point(3, 2), Point(3, 4), Point(2, 3), Point(4, 3)),
    ),
    TutorialPage(
        titleRes = R.string.tut_capture_title,
        bodyRes = R.string.tut_capture_body,
        rows = listOf(
            ".......",
            ".......",
            "...X...",
            "..XOX..",
            ".......",
            ".......",
            ".......",
        ),
        marks = setOf(Point(3, 4)),
    ),
    TutorialPage(
        titleRes = R.string.tut_atari_title,
        bodyRes = R.string.tut_atari_body,
        rows = listOf(
            "OOX..",
            ".X...",
            ".....",
            ".....",
            ".....",
        ),
        marks = setOf(Point(0, 1)),
    ),
    TutorialPage(
        titleRes = R.string.tut_illegal_title,
        bodyRes = R.string.tut_illegal_body,
        rows = listOf(
            ".......",
            ".......",
            "...X...",
            "..X.X..",
            "...X...",
            ".......",
            ".......",
        ),
        marks = setOf(Point(3, 3)),
    ),
    TutorialPage(
        titleRes = R.string.tut_ko_title,
        bodyRes = R.string.tut_ko_body,
        rows = listOf(
            ".XO..",
            "XO.O.",
            ".XO..",
            ".....",
            ".....",
        ),
        marks = setOf(Point(2, 1)),
    ),
    TutorialPage(
        titleRes = R.string.tut_pass_title,
        bodyRes = R.string.tut_pass_body,
        rows = listOf(
            ".......",
            ".......",
            "...X...",
            "...O...",
            ".......",
            ".......",
            ".......",
        ),
    ),
    TutorialPage(
        titleRes = R.string.tut_score_title,
        bodyRes = R.string.tut_score_body,
        rows = listOf(
            "XXXXX",
            "X...X",
            "X...X",
            "X...X",
            "XXXXX",
        ),
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorialScreen(onBack: () -> Unit) {
    var index by rememberSaveable { mutableIntStateOf(0) }
    val page = TutorialPages[index]
    val board = remember(page) { Board.fromAscii(page.rows) }

    Scaffold(
        topBar = { BackTopBar(title = stringResource(R.string.tutorial_title), onBack = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(
                    R.string.tutorial_page_of,
                    index + 1,
                    TutorialPages.size,
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(page.titleRes),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            GoBoard(
                board = board,
                lastMove = null,
                hint = null,
                deadStones = emptySet(),
                marks = page.marks,
                showCoordinates = page.showCoordinates,
                enabled = false,
                onTap = {},
                modifier = Modifier
                    .fillMaxWidth(0.78f)
                    .widthIn(max = 280.dp)
                    .align(Alignment.CenterHorizontally),
            )
            Text(
                text = stringResource(page.bodyRes),
                style = MaterialTheme.typography.bodyLarge,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (index > 0) {
                    OutlinedButton(onClick = { index-- }) {
                        Text(stringResource(R.string.tutorial_prev))
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        if (index == TutorialPages.lastIndex) onBack() else index++
                    },
                ) {
                    Text(
                        stringResource(
                            if (index == TutorialPages.lastIndex) {
                                R.string.tutorial_done
                            } else {
                                R.string.tutorial_next
                            },
                        ),
                    )
                }
            }
        }
    }
}
