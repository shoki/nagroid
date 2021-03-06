package de.schoar.nagroid.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.widget.AdapterView;
import android.widget.ListView;
import de.schoar.nagroid.DM;
import de.schoar.nagroid.nagios.NagiosExtState;
import de.schoar.nagroid.nagios.NagiosHost;
import de.schoar.nagroid.nagios.NagiosService;
import de.schoar.nagroid.nagios.NagiosState;

public class ProblemDialog extends AlertDialog {

	public ProblemDialog(Context context, AdapterView<?> view, int position) {
		super(context);
		init(view, position);
	}

	private void init(AdapterView<?> view, int position) {
		setTitle("Status details");

		final ListView lstSite = (ListView) view;
		Object problem = lstSite.getItemAtPosition(position);
		
		if (problem.getClass() == NagiosService.class) {
			final NagiosService serviceProblem = (NagiosService) problem;
			NagiosExtState serviceExtState = serviceProblem.getExtState();
			
			String msg = "Host: " + serviceProblem.getHost().getName() +
			   			 "\nService: " + serviceProblem.getName() +
			   			 "\nState: " + serviceProblem.getState();
			
			if (serviceExtState != null) {
				msg += "\nDuration: " + serviceProblem.getExtState().getDuration() +
					   "\nInfo: " + serviceProblem.getExtState().getInfo();
			}
			   
			setMessage(msg);
			
			if (serviceProblem.getState() != NagiosState.SERVICE_OK) {
				setButton3("Ack", new OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						new AcknowledgeDialog(lstSite.getContext(), serviceProblem).show();
					}
				});
				setButton2("Snooze", new OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						new SnoozeDialog(lstSite.getContext(), serviceProblem).show();
					}
				});	
			}
			setButton("View", new OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					Context context = lstSite.getContext();
					Uri uri = Uri.parse(DM.I.getConfiguration().getNagiosUrl()
							+ "/extinfo.cgi?type=2&host="+serviceProblem.getHost().getName()+"&service="+serviceProblem.getName());
					context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
				}
			});			
		}
		else if (problem.getClass() == NagiosHost.class) {
			final NagiosHost hostProblem = (NagiosHost) problem;
			
			setMessage("Host: " + hostProblem.getName() +
					   "\nState: " + hostProblem.getState() +
					   "\nService Problems: " + hostProblem.getServices().size());
			
			if (hostProblem.getState() != NagiosState.HOST_UP) {
				setButton3("Ack", new OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						new AcknowledgeDialog(lstSite.getContext(), hostProblem).show();
					}
				});
				setButton2("Actions", new OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						new SnoozeDialog(lstSite.getContext(), hostProblem).show();
					}
				});	
			}
			setButton("View", new OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					Context context = lstSite.getContext();
					Uri uri = Uri.parse(DM.I.getConfiguration().getNagiosUrl()
							+ "/status.cgi?host="+hostProblem.getName());
					context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
				}
			});		
		}
		
		setCancelable(true);
	}
}
