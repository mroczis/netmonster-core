
# NetMonster Core  
  
🚧 Work in progress! 🚧    
    
Lightweight Android library that is build over [Telephony SDK](https://developer.android.com/reference/android/telephony/package-summary). NetMonster core is extracted from [NetMonster](https://play.google.com/store/apps/details?id=cz.mroczis.netmonster) application and backports several Telephony features to older Android devices.  
  
Why use NetMonster Core instead of legacy API?  
 - Validation - library validates data from RIL and corrects them if possible 
 - Richer information - additional functions for cell identity and cell signal that will make your code more understandable  
 - Backport - several non-accessible signal or identity fields are now accessible without boilerplate code  
 - Tested - tested on real devices, 50 000+ active users  

### New functions
Here's small comparison for each of voice / data network you can meet.

#### GSM
|function    |Min SDK Android|Min SDK NetMonster Core |
|------------|---------------|------------------------|
|CGI         |-              |I (14)                  |
|NCC         |-              |N (24)                  |
|BCC         |-              |N (24)                  |
|Band        |-              |N (24)                  |
|TA          |O (26)         |N (24)                  |

#### WCDMA  
|function    |Min SDK Android|Min SDK NetMonster Core |  
|------------|---------------|------------------------|
|CGI         |-              |I (14)                  |
|CID (16b)   |-              |I (14)                  |  
|RNC         |-              |I (14)                  |
|Ec/Io       |-              |M (23)                  |  
|Band        |-              |N (24)                  |  
|BER         |-              |Q (29)                  |  
|Ec/No       |-              |Q (29)                  |  
|RSCP        |-              |Q (29)                  |

#### LTE
|function    |Min SDK Android|Min SDK NetMonster Core |
|------------|---------------|------------------------|
|eCGI        |-              |I (14)                  |
|CID (8b)    |-              |I (14)                  |
|eNb         |-              |I (14)                  | 
|RSSI        |Q (29)         |I (14)                  |
|RSRP        |O (26)         |I (14)                  |
|CQI         |O (26)         |I (14)                  |
|SNR         |O (26)         |I (14)                  |
|TA          |O (26)         |I (14)                  |
|Band        |-              |N (24)                  |  


### Usage

There are basically two ways you can use this library - as a validation library that will sanitize
data from AOSP cause lots of manufacturers modify source code and do not follow public documentation.
In that case you'll only need `ITelephonyManagerCompat` to retrieve AOSP-like models that are properly
validated.

The second option is to use advantages of additional postprocessing of NetMonster Core. As a result
you'll get more data but correctness is not 100 % guaranteed. 

#### Without additional postprocessing

NetMonster Core focuses on mapping of two AOSP's ways to fetch current cell information:
 - [TelephonyManager.getAllCellInfo()](https://developer.android.com/reference/android/telephony/TelephonyManager#getAllCellInfo())
 - [TelephonyManager.getCellLocation()](https://developer.android.com/reference/android/telephony/TelephonyManager.html#getCellLocation()) (deprecated in AOSP)
 - TelephonyManager.getNeighbouringCellInfo() (removed from AOSP)

Note that some of those methods are deprecated or even removed from AOSP - for more info see documentation of each method.

```kotlin
NetMonsterFactory.getTelephony(context, SUBSCRIPTION_ID).apply {
    val allCellInfo : List<ICell> = getAllCellInfo() 
    val cellLocation : List<ICell> = getCellLocation()
    val neighbouringCells : List<ICell> = getNeighbouringCellInfo()
}
```

#### Postprocessing

In this case you'll need to interact with `INetMonster` class. Here's list of problems 
that this library solves.

##### Merging data from multiple sources
Issue:
 - Android offers multiple ways how to get cell information.
 - Not all devices support one unified way how to access all the data.

Solution:
 - NetMonster Core grabs data from sources you specify, validates and merges them.

```kotlin
NetMonsterFactory.get(context).apply {
    val allSources : List<ICell> = getCells() // all sources
    val subset : List<ICell> = getCells( // subset of available sources
        CellSource.ALL_CELL_INFO, 
        CellSource.CELL_LOCATION
    ) 
}
```

##### Detection of LTE-A & HSPA+42
Issue:
 - AOSP cannot detect HSPA+42, only HSPA+.
 - AOSP does not offer a way to distinguish whether current network is using carrier aggregation or not.

Solution:
 - NetMonster Core attempts to guess HSPA+ 42 availability.
 - LTE-CA presence can be guessed based on cell info or detected using hidden APIs.

Using `getNetworkType(vararg detectors: INetworkDetector)` you can specify which `INetworkDetector` to use
when detecting current network type.

```kotlin
NetMonsterFactory.get(context).apply {
    // All detectors that are bundled in NetMonster Core
    val networkType : NetworkType = getNetworkType(SUBSCRIPTION_ID)
    
    // Only HSPA+42 (guess, not from RIL)
    val isHspaDc: NetworkType? = getNetworkType(SUBSCRIPTION_ID, DetectorHspaDc())
    // LTE-A from CellInfo (guess, not from RIL)
    val isLteCaCellInfo: NetworkType? = getNetworkType(SUBSCRIPTION_ID, DetectorLteAdvancedCellInfo())
    // LTE-A from ServiceState (from RIL, Android P+)
    val isLteCaServiceState: NetworkType? = getNetworkType(SUBSCRIPTION_ID, DetectorLteAdvancedNrServiceState())
    // LTE-A from PhysicalChannel (from RIL, Android P+)
    val isLteCaPhysicalChannel: NetworkType? = getNetworkType(SUBSCRIPTION_ID, DetectorLteAdvancedPhysicalChannel())
}
```

##### Other features
 - Detection of serving cells in 'emergency calls only' mode.
 - PLMN addition to non-serving cells in GSM, WCDMA, LTE, TD-SCDMA and NR networks.

License
-------

    Copyright 2019 Michal Mroček
    
    Licensed under the Apache License, Version 2.0 (the "License");

    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    

    http://www.apache.org/licenses/LICENSE-2.0

    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and