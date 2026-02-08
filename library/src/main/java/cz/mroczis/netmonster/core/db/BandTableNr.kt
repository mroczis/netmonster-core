package cz.mroczis.netmonster.core.db

import android.R
import cz.mroczis.netmonster.core.db.model.BandEntity
import cz.mroczis.netmonster.core.db.model.IBandEntity
import cz.mroczis.netmonster.core.model.band.BandNr

/**
 * In NR world loads of ARFCNs are overlapping.
 * This class holds all known bands for release 16.0.0 that have downlink defined.
 *
 * [3GPP 38.101-1 specification for NR](https://portal.3gpp.org/desktopmodules/Specifications/SpecificationDetails.aspx?specificationId=3283)
 */
object BandTableNr {

    private const val SMALLEST_BANDWIDTH = 5_000 // kHz

    private val bands = arrayOf(
        BandEntity(123_400..130_400, "600", 71, 15),
        BandEntity(143_400..145_600, "700", 29, 15),
        BandEntity(145_800..149_200, "700", 12, 15),
        BandEntity(151_600..160_600, "700", 28, 15),
        BandEntity(151_600..153_600, "700", 14, 15),
        BandEntity(158_200..164_200, "800", 20, 15),
        BandEntity(171_800..178_800, "850", 26, 15),
        BandEntity(172_000..175_000, "800", 18, 15),
        BandEntity(173_800..178_800, "850", 5, 15),
        BandEntity(185_000..192_000, "900", 8, 15),
        BandEntity(285_400..286_400, "1500", 51, 15),
        BandEntity(285_400..286_400, "1500", 76, 15),
        BandEntity(285_400..286_400, "1500", 93, 15),
        BandEntity(285_400..286_400, "1500", 91, 15),
        BandEntity(286_400..303_400, "1500", 50, 15),
        BandEntity(286_400..303_400, "1500", 75, 15),
        BandEntity(286_400..303_400, "1500", 92, 15),
        BandEntity(286_400..303_400, "1500", 94, 15),
        BandEntity(295_000..303_600, "1500", 74, 15),
        BandEntity(361_000..376_000, "1800", 3, 15),
        BandEntity(376_000..384_000, "1900", 39, 30),
        BandEntity(386_000..398_000, "1900", 2, 15),
        BandEntity(386_000..399_000, "1900", 25, 15),
        BandEntity(399_000..404_000, "AWS", 70, 15),
        BandEntity(402_000..405_000, "2000", 34, 30),
        BandEntity(422_000..440_000, "AWS", 66, 15),
        BandEntity(422_000..434_000, "2100", 1, 15),
        BandEntity(422_000..440_000, "2100", 65, 15),
        BandEntity(460_000..480_000, "2300", 40, 30),
        BandEntity(470_000..472_000, "2300", 30, 15),
        BandEntity(496_700..499_000, "2500", 53, 15),
        BandEntity(499_200..537_999, "2500", 41, 30),
        BandEntity(499_200..538_000, "2500", 90, 30),
        BandEntity(514_000..524_000, "2600", 38, 30),
        BandEntity(524_000..538_000, "2600", 7, 15),
        BandEntity(620_000..680_000, "3700", 77, 30),
        BandEntity(620_000..653_333, "3500", 78, 30),
        BandEntity(636_667..646_666, "3600", 48, 30),
        BandEntity(693_334..733_333, "4500", 79, 30),
        BandEntity(743_334..795_000, "5200", 46, 30),
        BandEntity(790_334..795_000, "5900", 47, 30),
        BandEntity(2_000_000..2_100_000, "28G", 257, 120),
        BandEntity(2_050_000..2_100_000, "28G", 261, 120),
        BandEntity(2_225_000..2_300_000, "39G", 260, 120),
    )

    /**
     * Lists all bands that do fit [arfcn] and are among [bandHints] (if not empty)
     */
    internal fun getAll(arfcn: Int, bandHints: IntArray = intArrayOf()) : List<BandEntity> =
        bands
            .filter { it.channelRange.contains(arfcn) }
            .filter { bandHints.isEmpty() || (it.number != null && bandHints.contains(it.number)) }

    /**
     * Prioritize n78/n28/n20/n1/n3/n7/n8, Disqualify n71/n25/n66/n260/n261
     */
    private val PRIORITY_EUROPE = listOf(1, 3, 7, 8, 20, 28, 78)

    /**
     * Prioritize n77/n71/n66/n25, Disqualify n78/n1/n3/n8/n28
     */
    private val PRIORITY_NORTH_AMERICA = listOf(25, 66, 71, 77)

    /**
     * Prioritize n77/n78/n79/n41/n1/n3
     */
    private val PRIORITY_ASIA = listOf(1, 3, 41, 78, 79)

    internal fun get(
        arfcn: Int,
        bandHints: IntArray = intArrayOf(),
        mcc: String? = null,
    ): BandEntity? {
        val candidates = getAll(arfcn = arfcn, bandHints = bandHints)

        if (candidates.isEmpty()) return null
        if (candidates.size == 1) return candidates[0]

        // Give priority by MCC -> continent(s)
        if (mcc != null) {
            val priorityBands = when {
                mcc.startsWith("mcc") || mcc.startsWith("31") || mcc.startsWith("33") -> PRIORITY_NORTH_AMERICA
                mcc.startsWith("2") -> PRIORITY_EUROPE
                mcc.startsWith("4") && mcc != "440" && mcc != "441" -> PRIORITY_ASIA
                mcc == "440" || mcc == "441" -> PRIORITY_ASIA + 77 // Japan uses n78 and n77
                else -> emptyList()
            }

            val continentCandidates = candidates.filter { it.number in priorityBands }

            // Filtered bands do not overlap (except Japan), no further processing implemented for now
            if (continentCandidates.size == 1) {
                return continentCandidates[0]
            }
        }

        when {
            candidates.isEmpty() -> return null
            candidates.size == 1 -> return candidates[0]
            else -> {
                // Multiple bands can contain specified arfcn.
                // Let's try find proper one using several small hacks.
                val filtered = candidates.filter { candidate ->
                    // In this snippet we assume 5 MHz as smallest bandwidth
                    // Note that not all bands do support 5 MHz BW but it's the smallest possible value
                    // Also we assume that there are no gaps non-5 MHz between blocks and 1st assigned one's
                    // arfcn is the lowest possible for given band
                    val startFrequency = getFrequency(candidate.channelRange.first)
                    val frequency = getFrequency(arfcn)

                    (startFrequency - frequency).rem(SMALLEST_BANDWIDTH) == 0
                }

                return if (filtered.isEmpty()) {
                    val uniqueName = candidates.distinctBy { it.name }
                    if (uniqueName.size == 1) {
                        // Safest bounds when it comes to bands - take min from start max from end
                        val min = candidates.minOf { it.channelRange.first }
                        val max = candidates.maxOf { it.channelRange.last }
                        uniqueName[0].copy(
                            channelRange = min..max,
                            number = null
                        )
                    } else {
                        null
                    }
                } else if (filtered.size == 1) {
                    filtered[0]
                } else {
                    // Multiple bands do fit - generally this can happen in real world
                    // If at least names of all bands match we'll return a bit accurate data...
                    val uniqueName = filtered.distinctBy { it.name }
                    if (uniqueName.size == 1) {
                        uniqueName[0].copy(number = null)
                    } else {
                        null
                    }
                }
            }
        }
    }

    /**
     * Calculates frequency from arfcn.
     *
     * Taken from 3GPP 38.101-1 / 5.4.2.1 NR-ARFCN and channel raster
     * @return downlink in kHz
     */
    private fun getFrequency(arfcn: Int): Int {
        return if (arfcn <= 600_000) {
            5 * arfcn
        } else {
            3_000_000 + 15 * (arfcn - 600_000)
        }
    }

    /**
     * Attempts to find *ALL* bands that do fit ARFCN + bandHints
     */
    fun mapAll(
        arfcn: Int,
        bandHints: IntArray = intArrayOf(),
    ) : List<BandNr> = getAll(arfcn = arfcn, bandHints = bandHints).map { it.toBandNr(arfcn = arfcn) }

    /**
     * Attempts to find current band information depending on [arfcn].
     * If no such band is found or there are multiple candidates then result [BandNr] will contain only [BandNr.downlinkArfcn].
     */
    fun map(
        arfcn: Int,
        bandHints: IntArray = intArrayOf(),
        mcc: String? = null,
    ): BandNr = get(arfcn, bandHints, mcc).toBandNr(arfcn = arfcn)

    private fun BandEntity?.toBandNr(arfcn: Int) = BandNr(
        downlinkArfcn = arfcn,
        downlinkFrequency = getFrequency(arfcn),
        number = this?.number,
        name = this?.name,
        expectedScs = this?.scs,
    )

}

