package com.couchbase.mobile.wmreplicator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.couchbase.mobile.wmreplicator.ui.theme.WMReplicatorTheme
import com.couchbase.mobile.wmreplicator.vm.UIState
import com.couchbase.mobile.wmreplicator.vm.WMRViewModel
import org.koin.androidx.viewmodel.ext.android.getViewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vm = getViewModel<WMRViewModel>()
        vm.connect(this)
        setContent {
            WMReplicatorTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    ReplScreen(this, vm)
                }
            }
        }
    }
}

@Composable
fun ReplScreen(ctxt: ComponentActivity?, vm: WMRViewModel) {
    val state: UIState by vm.uiState.collectAsState()
    ReplScreenContent(ctxt, vm, state)
}

@Composable
fun ReplScreenContent(ctxt: ComponentActivity?, vm: WMRViewModel?, state: UIState) {
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(96.dp))
        Row(modifier = Modifier.fillMaxWidth().weight(0.3f)) {
            Column(Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    enabled = state.connected && !state.running,
                    onClick = { ctxt?.let { vm?.start(it) } }) {
                    Text("Start")
                }
                Button(
                    modifier = Modifier.padding(top = 12.dp),
                    enabled = state.connected && state.running,
                    onClick = { ctxt?.let { vm?.stop(it) } }) {
                    Text("Stop")
                }
                Row(modifier = Modifier.padding(top = 12.dp),
                    horizontalArrangement = Arrangement.Center) {
                    Text(state.progress, modifier = Modifier.padding(12.dp))
                    Text(state.state, modifier = Modifier.padding(12.dp))
                }
            }

        }
        Row(modifier = Modifier.fillMaxWidth().weight(.3f)) {
            Text(state.error, maxLines = 20, modifier = Modifier.padding(12.dp).fillMaxSize())
        }
        Row(modifier = Modifier.fillMaxWidth().weight(.3f)) {
            Spacer(modifier = Modifier.height(12.dp))
        }

    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WMReplicatorTheme {
        ReplScreenContent(null, null, UIState(true, true, "It's all good", "BUSY", "24/236"))
    }
}
