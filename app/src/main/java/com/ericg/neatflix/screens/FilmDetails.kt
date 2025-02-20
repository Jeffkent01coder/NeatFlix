package com.ericg.neatflix.screens

import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale.Companion.Crop
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.Light
import androidx.compose.ui.text.font.FontWeight.Companion.Normal
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.ericg.neatflix.R
import com.ericg.neatflix.data.local.MyListMovie
import com.ericg.neatflix.model.Cast
import com.ericg.neatflix.model.Film
import com.ericg.neatflix.model.Genre
import com.ericg.neatflix.sharedComposables.MovieGenreChip
import com.ericg.neatflix.ui.theme.AppOnPrimaryColor
import com.ericg.neatflix.ui.theme.AppPrimaryColor
import com.ericg.neatflix.ui.theme.ButtonColor
import com.ericg.neatflix.util.Constants.BASE_BACKDROP_IMAGE_URL
import com.ericg.neatflix.util.Constants.BASE_POSTER_IMAGE_URL
import com.ericg.neatflix.viewmodel.DetailsViewModel
import com.ericg.neatflix.viewmodel.HomeViewModel
import com.ericg.neatflix.viewmodel.WatchListViewModel
import com.gowtham.ratingbar.RatingBar
import com.gowtham.ratingbar.RatingBarConfig
import com.gowtham.ratingbar.RatingBarStyle
import com.gowtham.ratingbar.StepSize
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.skydoves.landscapist.CircularReveal
import com.skydoves.landscapist.ShimmerParams
import com.skydoves.landscapist.coil.CoilImage
import java.text.SimpleDateFormat
import java.util.*

@Destination
@Composable
fun MovieDetails(
    navigator: DestinationsNavigator,
    homeViewModel: HomeViewModel = hiltViewModel(),
    detailsViewModel: DetailsViewModel = hiltViewModel(),
    watchListViewModel: WatchListViewModel = hiltViewModel(),
    currentFilm: Film
) {
    var film by remember {
        mutableStateOf(currentFilm)
    }

    val date = SimpleDateFormat.getDateTimeInstance().format(Date())
    val watchListMovie = MyListMovie(
        mediaId = film.id,
        imagePath = film.posterPath,
        title = film.title,
        releaseDate = film.releaseDate,
        rating = film.voteAverage,
        addedOn = date
    )

    val addedToList = watchListViewModel.addedToWatchList.value
    val similarFilms = detailsViewModel.similarMovies.value.collectAsLazyPagingItems()
    val movieCastList = detailsViewModel.movieCast.value
    val filmType = homeViewModel.selectedFilmType.value

    LaunchedEffect(key1 = film) {
        detailsViewModel.getSimilarFilms(filmId = film.id, filmType)
        detailsViewModel.getFilmCast(filmId = film.id, filmType)
        watchListViewModel.exists(mediaId = film.id)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF180E36))
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.33F)
        ) {
            val (
                backdropImage,
                backButton,
                movieTitleBox,
                moviePosterImage,
                translucentBr
            ) = createRefs()

            CoilImage(
                imageModel = "$BASE_BACKDROP_IMAGE_URL/${film.backdropPath}",
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .fillMaxHeight()
                    .constrainAs(backdropImage) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    },
                failure = {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.backdrop_not_available),
                            contentDescription = "no image"
                        )
                    }
                },
                shimmerParams = ShimmerParams(
                    baseColor = AppPrimaryColor,
                    highlightColor = ButtonColor,
                    durationMillis = 500,
                    dropOff = 0.65F,
                    tilt = 20F
                ),
                contentScale = Crop,
                contentDescription = "Header backdrop image",
            )

            ConstraintLayout(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(ButtonColor.copy(alpha = 0.78F))
                    .constrainAs(backButton) {
                        top.linkTo(parent.top, margin = 16.dp)
                        start.linkTo(parent.start, margin = 10.dp)
                    }
            ) {
                val (icon) = createRefs()
                IconButton(onClick = {
                    navigator.navigateUp()
                }) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "back button",
                        tint = AppOnPrimaryColor,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp)
                            .constrainAs(icon) {
                                top.linkTo(parent.top)
                                bottom.linkTo(parent.bottom)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                            }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color(0XFF180E36).copy(alpha = 0.5F),
                                Color(0XFF180E36)
                            ),
                            startY = 0.1F
                        )
                    )
                    .constrainAs(translucentBr) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(backdropImage.bottom)
                    }
            )

            Column(
                modifier = Modifier.constrainAs(movieTitleBox) {
                    start.linkTo(moviePosterImage.end, margin = 12.dp)
                    end.linkTo(parent.end, margin = 12.dp)
                    bottom.linkTo(moviePosterImage.bottom, margin = 10.dp)
                },
                verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.Start
            ) {

                var paddingValue by remember { mutableStateOf(2) }
                Text(
                    text = when (film.mediaType) {
                        "tv" -> {
                            paddingValue = 2
                            "Series"
                        }
                        "movie" -> {
                            paddingValue = 2
                            "Movie"
                        }
                        else -> {
                            paddingValue = 0
                            ""
                        }
                    },
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(size = 4.dp))
                        .background(Color.DarkGray.copy(alpha = 0.5F))
                        .padding(paddingValue.dp),
                    color = AppOnPrimaryColor.copy(alpha = 0.78F),
                    fontSize = 12.sp,
                )

                Text(
                    text = film.title,
                    modifier = Modifier
                        .padding(top = 2.dp, start = 4.dp, bottom = 8.dp)
                        .fillMaxWidth(0.5F),
                    maxLines = 2,
                    fontSize = 18.sp,
                    fontWeight = Bold,
                    color = Color.White.copy(alpha = 0.78F)
                )

                Text(
                    text = film.releaseDate,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
                    fontSize = 15.sp,
                    fontWeight = Light,
                    color = Color.White.copy(alpha = 0.56F)
                )

                RatingBar(
                    value = (film.voteAverage / 2).toFloat(),
                    modifier = Modifier.padding(horizontal = 6.dp),
                    config = RatingBarConfig()
                        .style(RatingBarStyle.Normal)
                        .isIndicator(true)
                        .activeColor(Color(0XFFC9F964))
                        .hideInactiveStars(false)
                        .inactiveColor(Color.LightGray.copy(alpha = 0.3F))
                        .stepSize(StepSize.HALF)
                        .numStars(5)
                        .size(16.dp)
                        .padding(4.dp),
                    onValueChange = {},
                    onRatingChanged = {}
                )

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(start = 4.dp, bottom = 8.dp)
                        .fillMaxWidth(0.42F),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .border(
                                1.dp,
                                color = if (film.adult) Color(0xFFFF6F6F) else Color.White.copy(
                                    alpha = 0.78F
                                )
                            )
                            .background(if (film.adult) Color(0xFFFF6F6F).copy(alpha = 0.14F) else Color.Transparent)
                            .padding(4.dp)
                    ) {
                        val color: Color
                        Text(
                            text = if (film.adult) {
                                color = Color(0xFFFF6F6F)
                                "18+"
                            } else {
                                color = Color.White.copy(alpha = 0.56F)
                                "PG"
                            },
                            fontSize = 14.sp,
                            fontWeight = Normal,
                            color = color
                        )
                    }

                    val context = LocalContext.current
                    // val scope = rememberCoroutineScope()
                    IconButton(onClick = {
                        if (addedToList != 0) {
                            watchListViewModel.removeFromWatchList(watchListMovie.mediaId)
                            Toast.makeText(
                                context, "Removed from watchlist", LENGTH_SHORT
                            ).show()

                        } else {
                            watchListViewModel.addToWatchList(watchListMovie)
                            Toast.makeText(
                                context, "Added to watchlist", LENGTH_SHORT
                            ).show()

                        }
                    }) {
                        Icon(
                            painter = painterResource(
                                id = if (addedToList != 0) R.drawable.ic_added_to_list
                                else R.drawable.ic_add_to_list
                            ),
                            tint = AppOnPrimaryColor,
                            contentDescription = "add to watch list icon"
                        )
                    }
                }
            }

            CoilImage(
                imageModel = "$BASE_POSTER_IMAGE_URL/${film.posterPath}",
                modifier = Modifier
                    .padding(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .width(115.dp)
                    .height(172.5.dp)
                    .constrainAs(moviePosterImage) {
                        top.linkTo(backdropImage.bottom)
                        bottom.linkTo(backdropImage.bottom)
                        start.linkTo(parent.start)
                    }, failure = {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.image_not_available),
                            contentDescription = "no image"
                        )
                    }
                },
                shimmerParams = ShimmerParams(
                    baseColor = AppPrimaryColor,
                    highlightColor = ButtonColor,
                    durationMillis = 500,
                    dropOff = 0.65F,
                    tilt = 20F
                ),
                previewPlaceholder = R.drawable.popcorn,
                contentScale = Crop,
                circularReveal = CircularReveal(duration = 1000),
                contentDescription = "movie poster"
            )
        }

        LazyRow(
            modifier = Modifier
                .padding(top = (96).dp, bottom = 4.dp, start = 4.dp, end = 4.dp)
                .fillMaxWidth()
        ) {
            val filmGenres: List<Genre> = homeViewModel.filmGenres.filter { genre ->
                return@filter if (film.genreIds.isNullOrEmpty()) false else
                    film.genreIds!!.contains(genre.id)
            }
            filmGenres.forEach { genre ->
                item {
                    MovieGenreChip(
                        background = ButtonColor,
                        textColor = AppOnPrimaryColor,
                        genre = genre.name
                    )
                }
            }
        }

        ExpandableText(
            text = film.overview,
            modifier = Modifier
                .padding(top = 3.dp, bottom = 4.dp, start = 4.dp, end = 4.dp)
                .fillMaxWidth()
        )

        LazyColumn(
            horizontalAlignment = Alignment.Start
        ) {
            item {
                AnimatedVisibility(visible = (movieCastList.isNotEmpty())) {
                    Text(
                        text = "Cast",
                        fontWeight = Bold,
                        fontSize = 18.sp,
                        color = AppOnPrimaryColor,
                        modifier = Modifier.padding(start = 4.dp, top = 6.dp, bottom = 4.dp)
                    )
                }
            }
            item {
                LazyRow(modifier = Modifier.padding(4.dp)) {
                    movieCastList.forEach { cast ->
                        item { CastMember(cast = cast) }
                    }
                }
            }
            item {
                if (similarFilms.itemCount != 0) {
                    Text(
                        text = "Similar",
                        fontWeight = Bold,
                        fontSize = 18.sp,
                        color = AppOnPrimaryColor,
                        modifier = Modifier.padding(start = 4.dp, top = 6.dp, bottom = 4.dp)
                    )
                }
            }

            item {
                LazyRow(modifier = Modifier.fillMaxWidth()) {
                    items(similarFilms) { thisMovie ->
                        CoilImage(
                            imageModel = "${BASE_POSTER_IMAGE_URL}/${thisMovie!!.posterPath}",
                            shimmerParams = ShimmerParams(
                                baseColor = AppPrimaryColor,
                                highlightColor = ButtonColor,
                                durationMillis = 500,
                                dropOff = 0.65F,
                                tilt = 20F
                            ),
                            failure = {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.image_not_available),
                                        contentDescription = "no image"
                                    )
                                }
                            },
                            previewPlaceholder = R.drawable.popcorn,
                            contentScale = Crop,
                            circularReveal = CircularReveal(duration = 1000),
                            modifier = Modifier
                                .padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .size(130.dp, 195.dp)
                                .clickable {
                                    film = thisMovie
                                },
                            contentDescription = "Movie item"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CastMember(cast: Cast?) {
    Column(
        modifier = Modifier.padding(end = 8.dp, top = 2.dp, bottom = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CoilImage(
            modifier = Modifier
                .clip(CircleShape)
                .size(70.dp),
            imageModel = "$BASE_POSTER_IMAGE_URL/${cast!!.profilePath}",
            shimmerParams = ShimmerParams(
                baseColor = AppPrimaryColor,
                highlightColor = ButtonColor,
                durationMillis = 500,
                dropOff = 0.65F,
                tilt = 20F
            ),
            failure = {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        modifier = Modifier.size(70.dp),
                        painter = painterResource(id = R.drawable.ic_user),
                        tint = Color.LightGray,
                        contentDescription = null
                    )
                }
            },
            previewPlaceholder = R.drawable.ic_user,
            contentScale = Crop,
            circularReveal = CircularReveal(duration = 1000),
            contentDescription = "cast image"
        )
        Text(
            text = trimName(cast.name),
            maxLines = 1,
            color = AppOnPrimaryColor.copy(alpha = 0.5F),
            fontSize = 14.sp,
        )
        Text(
            text = trimName(cast.department),
            maxLines = 1,
            color = AppOnPrimaryColor.copy(alpha = 0.45F),
            fontSize = 12.sp,
        )
    }
}

fun trimName(name: String): String {
    return if (name.length <= 10) name else {
        name.removeRange(8..name.lastIndex) + "..."
    }
}

@Composable
fun ExpandableText(
    text: String,
    modifier: Modifier = Modifier,
    minimizedMaxLines: Int = 2,
) {
    var cutText by remember(text) { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val textLayoutResultState = remember { mutableStateOf<TextLayoutResult?>(null) }
    val seeMoreSizeState = remember { mutableStateOf<IntSize?>(null) }
    val seeMoreOffsetState = remember { mutableStateOf<Offset?>(null) }

    // getting raw values for smart cast
    val textLayoutResult = textLayoutResultState.value
    val seeMoreSize = seeMoreSizeState.value
    val seeMoreOffset = seeMoreOffsetState.value

    LaunchedEffect(text, expanded, textLayoutResult, seeMoreSize) {
        val lastLineIndex = minimizedMaxLines - 1
        if (!expanded && textLayoutResult != null && seeMoreSize != null &&
            lastLineIndex + 1 == textLayoutResult.lineCount &&
            textLayoutResult.isLineEllipsized(lastLineIndex)
        ) {
            var lastCharIndex = textLayoutResult.getLineEnd(lastLineIndex, visibleEnd = true) + 1
            var charRect: Rect
            do {
                lastCharIndex -= 1
                charRect = textLayoutResult.getCursorRect(lastCharIndex)
            } while (
                charRect.left > textLayoutResult.size.width - seeMoreSize.width
            )
            seeMoreOffsetState.value = Offset(charRect.left, charRect.bottom - seeMoreSize.height)
            cutText = text.substring(startIndex = 0, endIndex = lastCharIndex)
        }
    }

    Box(modifier) {
        Text(
            color = AppOnPrimaryColor,
            text = cutText ?: text,
            modifier = Modifier
                .clickable(
                    interactionSource = MutableInteractionSource(),
                    indication = null
                ) {
                    if (expanded) {
                        expanded = false
                    }
                },
            maxLines = if (expanded) Int.MAX_VALUE else minimizedMaxLines,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { textLayoutResultState.value = it },
        )

        if (!expanded) {
            val density = LocalDensity.current
            Text(
                color = Color(0x2DFF978C).copy(alpha = 0.78F),
                text = "... See more",
                fontWeight = Bold,
                fontSize = 14.sp,
                onTextLayout = { seeMoreSizeState.value = it.size },
                modifier = Modifier
                    .then(
                        if (seeMoreOffset != null)
                            Modifier.offset(
                                x = with(density) { seeMoreOffset.x.toDp() },
                                y = with(density) { seeMoreOffset.y.toDp() },
                            )
                        else Modifier
                    )
                    .clickable(
                        interactionSource = MutableInteractionSource(),
                        indication = null
                    ) {
                        expanded = true
                        cutText = null
                    }
                    .alpha(if (seeMoreOffset != null) 1f else 0f)
                    .verticalScroll(
                        enabled = true,
                        state = rememberScrollState()
                    )
            )
        }
    }
}
