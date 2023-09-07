from distributor.cleaners.cell import get_cleaned_cellular_data
from distributor.cleaners.wifi import get_cleaned_wifi_data
from distributor.cleaners.action import get_cleaned_action_data
from distributor.cleaners.bluetooth import get_cleaned_bluetooth_data
from client.application import f_update_twin_instances



def f_handler_wifi(data:str)->None:
    cleaned=get_cleaned_wifi_data(data)
    f_update_twin_instances(cleaned, "BSSID", "dtmi:network:wifi;1", 'wifi')
    return 


def f_handler_cell(data:str)->None:
    cleaned=get_cleaned_cellular_data(data)
    f_update_twin_instances(cleaned, 'globalId', "dtmi:network:cell;1", 'cell')
    return 


def f_handler_action(data:str)->None:
    cleaned=get_cleaned_action_data(data)
    f_update_twin_instances(cleaned, None, None, None)
    print("Aboreted")
    return


def f_handler_bluetooth(data:str)->None:
    cleaned=get_cleaned_bluetooth_data(data)
    f_update_twin_instances(cleaned, 'MAC', "dtmi:network:bluetooth;1", 'bluetooth')
    return

