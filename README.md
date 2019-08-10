
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

NetMonster Core focuses on mapping of two AOSP's ways to fetch current cell information:
 - [TelephonyManager.getAllCellInfo()](https://developer.android.com/reference/android/telephony/TelephonyManager#getAllCellInfo())
 - [TelephonyManager.getCellLocation()](https://developer.android.com/reference/android/telephony/TelephonyManager.html#getCellLocation()) (deprecated in AOSP)

Whilst using NetMonster Core you just need to retrieve instance of `TelephonyManagerCompat` and
call method whose name corresponds to AOSP's one.

```kotlin
TelephonyManagerCompat.getInstance(this).apply {
    getAllCellInfo { cellList : List<ICell> ->
        
    }

    val cellLocation : List<ICell> = getCellLocation()
}
```
    
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