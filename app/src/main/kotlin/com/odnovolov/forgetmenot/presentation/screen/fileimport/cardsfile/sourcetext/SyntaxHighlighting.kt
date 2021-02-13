package com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile.sourcetext

import com.brackeys.ui.language.base.Language
import com.brackeys.ui.language.base.model.ParseResult
import com.brackeys.ui.language.base.model.Suggestion
import com.brackeys.ui.language.base.model.SyntaxScheme
import com.brackeys.ui.language.base.parser.LanguageParser
import com.brackeys.ui.language.base.provider.SuggestionProvider
import com.brackeys.ui.language.base.span.StyleSpan
import com.brackeys.ui.language.base.span.SyntaxHighlightSpan
import com.brackeys.ui.language.base.styler.LanguageStyler
import com.brackeys.ui.language.base.utils.StylingResult
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImporter
import com.odnovolov.forgetmenot.domain.interactor.fileimport.Parser
import com.odnovolov.forgetmenot.presentation.common.businessLogicThread
import kotlinx.coroutines.*

class SyntaxHighlighting(
    private val filerImporter: FileImporter
) : Language {
    private var parsing: Job? = null
    private val coroutineScope = CoroutineScope(businessLogicThread)

    override fun getStyler(): LanguageStyler {
        return object : LanguageStyler {
            override fun cancel() {
                parsing?.cancel()
            }

            override fun enqueue(
                sourceCode: String,
                syntaxScheme: SyntaxScheme,
                stylingResult: StylingResult
            ) {
                parsing = coroutineScope.launch {
                    val parserResult: Parser.ParserResult = filerImporter.updateText(sourceCode)
                    val spans: MutableList<SyntaxHighlightSpan> = ArrayList()
                    for (cardMarkup in parserResult.cardMarkups) {
                        cardMarkup.questionRange?.let { questionRange: IntRange ->
                            val questionSpan = SyntaxHighlightSpan(
                                StyleSpan(syntaxScheme.operatorColor, bold = true),
                                questionRange.first,
                                questionRange.last + 1
                            )
                            spans.add(questionSpan)
                        }
                        cardMarkup.answerRange?.let { answerRange: IntRange ->
                            val answerSpan = SyntaxHighlightSpan(
                                StyleSpan(syntaxScheme.keywordColor, bold = true),
                                answerRange.first,
                                answerRange.last + 1
                            )
                            spans.add(answerSpan)
                        }
                    }
                    withContext(Dispatchers.Main) {
                        stylingResult.invoke(spans)
                    }
                }
            }

            override fun execute(
                sourceCode: String,
                syntaxScheme: SyntaxScheme
            ): List<SyntaxHighlightSpan> {
                throw UnsupportedOperationException()
            }

        }
    }

    fun dispose() {
        coroutineScope.cancel()
    }

    override fun getName(): String = "CardsFileSyntaxHighlighting"
    override fun getParser(): LanguageParser = EmptyParser
    override fun getProvider(): SuggestionProvider = EmptySuggestionProvider

    private companion object {
        val EmptyParser = object : LanguageParser {
            override fun execute(name: String, source: String): ParseResult = ParseResult(null)
        }

        val EmptySuggestionProvider = object : SuggestionProvider {
            override fun clearLines() {}
            override fun deleteLine(lineNumber: Int) {}
            override fun getAll(): Set<Suggestion> = emptySet()
            override fun processLine(lineNumber: Int, text: String) {}
        }
    }
}